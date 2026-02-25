#!/usr/bin/env python3
"""
Parse local C 100-question docx, estimate difficulty, and generate MySQL import SQL.
The SQL upserts questions, creates generic Chinese tags, and binds many-to-many relations.
"""

from __future__ import annotations

import argparse
import re
import zipfile
from pathlib import Path
from typing import Dict, List, Optional, Tuple

QUESTION_HEADER_RE = re.compile(r"^(\d{1,3})[\.．、]\s*(.+)$")
MAX_QUESTIONS = 100

CHAPTER_RULES: List[Tuple[str, str, List[str]]] = [
    ("string", "字符串处理", ["字符串", "字符数组", "char", "str", "strlen", "strcmp"]),
    ("array", "数组与矩阵", ["数组", "矩阵", "二维", "一维", "排序", "查找"]),
    ("pointer", "指针基础", ["指针", "地址", "malloc", "free"]),
    ("function", "函数与递归", ["函数", "递归", "形参", "实参"]),
    ("data_structure", "数据结构基础", ["链表", "队列", "栈", "哈希", "二叉树", "图"]),
    ("file_io", "文件输入输出", ["文件", "fopen", "fclose", "fscanf", "fprintf", "磁盘"]),
    ("basic", "基础语法", ["循环", "分支", "输入", "输出", "条件", "for", "while", "if", "switch"]),
]

KNOWLEDGE_RULES: List[Tuple[str, List[str]]] = [
    ("数组", ["数组", "矩阵", "二维", "下标"]),
    ("字符串", ["字符串", "字符数组", "char", "str", "strlen", "strcmp"]),
    ("哈希", ["哈希", "散列", "hash"]),
    ("队列", ["队列"]),
    ("栈", ["栈", "后进先出", "lifo"]),
    ("链表", ["链表", "结点", "节点"]),
    ("指针", ["指针", "地址", "malloc", "free"]),
    ("递归", ["递归"]),
    ("排序", ["排序", "冒泡", "快速排序", "插入排序", "选择排序"]),
    ("查找", ["查找", "搜索", "二分"]),
    ("函数", ["函数", "形参", "实参", "返回值"]),
    ("文件", ["文件", "fopen", "fclose", "fscanf", "fprintf", "磁盘"]),
    ("数学计算", ["素数", "水仙花", "公约数", "公倍数", "sin", "cos", "方程"]),
    ("循环与分支", ["for", "while", "if", "switch", "break", "continue"]),
]

CHAPTER_TO_KNOWLEDGE_FALLBACK = {
    "string": "字符串",
    "array": "数组",
    "pointer": "指针",
    "function": "函数",
    "data_structure": "链表",
    "file_io": "文件",
    "basic": "循环与分支",
}


def load_docx_lines(docx_path: Path) -> List[str]:
    with zipfile.ZipFile(docx_path) as zf:
        xml = zf.read("word/document.xml").decode("utf-8")

    text = re.sub(r"<w:tab[^>]*/>", "\t", xml)
    text = re.sub(r"</w:p>", "\n", text)
    text = re.sub(r"<[^>]+>", "", text)
    text = text.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
    text = text.replace("\u3000", " ")
    return [line.strip() for line in text.splitlines() if line.strip()]


def parse_questions(lines: List[str]) -> List[Dict[str, str]]:
    questions: List[Dict[str, str]] = []
    current_no: Optional[int] = None
    current_title = ""
    body: List[str] = []

    def flush() -> None:
        nonlocal current_no, current_title, body
        if current_no is None:
            return
        questions.append(
            {
                "no": str(current_no),
                "stem": current_title.strip(),
                "analysis": "\n".join(body).strip(),
            }
        )

    for line in lines:
        m = QUESTION_HEADER_RE.match(line)
        if m:
            no = int(m.group(1))
            if 1 <= no <= MAX_QUESTIONS:
                flush()
                current_no = no
                current_title = m.group(2).strip()
                body = []
                continue
        if current_no is not None:
            body.append(line)

    flush()

    by_no: Dict[int, Dict[str, str]] = {}
    for q in questions:
        no = int(q["no"])
        if no not in by_no:
            by_no[no] = q
    return [by_no[i] for i in sorted(by_no.keys()) if 1 <= i <= MAX_QUESTIONS]


def pick_chapter_key(stem: str, analysis: str) -> str:
    text = f"{stem}\n{analysis}".lower()
    for key, _, keywords in CHAPTER_RULES:
        if key == "basic":
            continue
        if any(keyword.lower() in text for keyword in keywords):
            return key
    return "basic"


def chapter_label(chapter_key: str) -> str:
    for key, label, _ in CHAPTER_RULES:
        if key == chapter_key:
            return label
    return "基础语法"


def estimate_difficulty(no: int, stem: str, analysis: str) -> int:
    text = f"{stem}\n{analysis}".lower()
    if no <= 15:
        diff = 1
    elif no <= 50:
        diff = 2
    elif no <= 80:
        diff = 3
    else:
        diff = 4

    medium_keywords = ["数组", "字符串", "矩阵", "函数", "排序", "查找"]
    hard_keywords = ["递归", "指针", "链表", "队列", "栈", "哈希", "文件", "二叉树"]
    easy_keywords = ["求和", "打印", "交换", "最大公约数", "最小公倍数", "水仙花"]

    if any(k in text for k in medium_keywords):
        diff += 1
    if any(k in text for k in hard_keywords):
        diff += 1
    if len(analysis) > 450:
        diff += 1
    if any(k in text for k in easy_keywords):
        diff -= 1
    return max(1, min(5, diff))


def detect_knowledge_tags(stem: str, analysis: str, chapter_key: str) -> List[str]:
    text = f"{stem}\n{analysis}".lower()
    matched: List[str] = []
    for tag_name, keywords in KNOWLEDGE_RULES:
        if any(keyword.lower() in text for keyword in keywords):
            matched.append(tag_name)

    if not matched:
        matched.append(CHAPTER_TO_KNOWLEDGE_FALLBACK.get(chapter_key, "循环与分支"))

    seen = set()
    result: List[str] = []
    for name in matched:
        if name not in seen:
            seen.add(name)
            result.append(name)
    return result


def sql_escape(text: str) -> str:
    return text.replace("\\", "\\\\").replace("'", "''")


def tag_upsert_sql(
    tag_name: str,
    tag_code: str,
    parent_expr: str,
    tag_level: int,
    tag_type: int,
    sort_order: int,
) -> List[str]:
    update_stmt = (
        "UPDATE qb_tag t SET "
        f"t.tag_code='{sql_escape(tag_code)}', t.parent_id={parent_expr}, t.tag_level={tag_level}, "
        f"t.tag_type={tag_type}, t.sort_order={sort_order}, t.is_deleted=0, t.updated_at=NOW(3) "
        f"WHERE t.tag_name='{sql_escape(tag_name)}' "
        "AND NOT EXISTS (SELECT 1 FROM qb_tag_mastery tm WHERE tm.tag_id=t.id);"
    )

    return [
        (
            "INSERT INTO qb_tag(tag_name, tag_code, parent_id, tag_level, tag_type, sort_order, created_at, updated_at, is_deleted)\n"
            f"SELECT '{sql_escape(tag_name)}', '{sql_escape(tag_code)}', {parent_expr}, {tag_level}, {tag_type}, {sort_order}, NOW(3), NOW(3), 0\n"
            "FROM DUAL\n"
            f"WHERE NOT EXISTS (SELECT 1 FROM qb_tag WHERE tag_name='{sql_escape(tag_name)}');"
        ),
        update_stmt,
    ]


def relation_sql(question_title: str, tag_name: str) -> str:
    return (
        "INSERT INTO qb_question_tag(question_id, tag_id, created_at)\n"
        "SELECT q.id, t.id, NOW(3)\n"
        "FROM qb_question q\n"
        "JOIN qb_tag t ON t.tag_name='"
        + sql_escape(tag_name)
        + "' AND t.is_deleted=0\n"
        "LEFT JOIN qb_question_tag qt ON qt.question_id=q.id AND qt.tag_id=t.id\n"
        "WHERE q.title='"
        + sql_escape(question_title)
        + "' AND q.is_deleted=0 AND qt.question_id IS NULL;"
    )


def build_sql(questions: List[Dict[str, str]]) -> str:
    lines: List[str] = []
    lines.append("SET NAMES utf8mb4;")
    lines.append("START TRANSACTION;")
    lines.append("SET @admin_id := (SELECT id FROM sys_user WHERE username='admin' AND is_deleted=0 LIMIT 1);")

    lines.append("-- cleanup old C100-specific tags and old C100 relations")
    lines.append("DELETE qt FROM qb_question_tag qt JOIN qb_question q ON q.id=qt.question_id WHERE q.title LIKE 'C100-%';")
    lines.append(
        "DELETE qt FROM qb_question_tag qt JOIN qb_tag t ON t.id=qt.tag_id "
        "WHERE t.tag_name LIKE 'C100-%' OR t.tag_name IN ('题库-C100','章节-C100','难度-C100') "
        "OR t.tag_code IN ('CHAPTER','KNOWLEDGE','DIFFICULTY') "
        "OR t.tag_code LIKE 'CHAPTER_%' OR t.tag_code LIKE 'KNOW_%' OR t.tag_code LIKE 'DIFF_%';"
    )
    lines.append(
        "DELETE tm FROM qb_tag_mastery tm JOIN qb_tag t ON t.id=tm.tag_id "
        "WHERE t.tag_name LIKE 'C100-%' OR t.tag_name IN ('题库-C100','章节-C100','难度-C100') "
        "OR t.tag_code IN ('CHAPTER','KNOWLEDGE','DIFFICULTY') "
        "OR t.tag_code LIKE 'CHAPTER_%' OR t.tag_code LIKE 'KNOW_%' OR t.tag_code LIKE 'DIFF_%';"
    )
    lines.append(
        "DELETE FROM qb_tag "
        "WHERE tag_name LIKE 'C100-%' OR tag_name IN ('题库-C100','章节-C100','难度-C100') "
        "OR tag_code IN ('CHAPTER','KNOWLEDGE','DIFFICULTY') "
        "OR tag_code LIKE 'CHAPTER_%' OR tag_code LIKE 'KNOW_%' OR tag_code LIKE 'DIFF_%';"
    )

    imported_rows = []
    for item in questions:
        no = int(item["no"])
        stem = item["stem"].strip()
        analysis = item["analysis"].strip()

        chapter_key = pick_chapter_key(stem, analysis)
        chapter_name = chapter_label(chapter_key)
        difficulty = estimate_difficulty(no, stem, analysis)
        knowledge_tags = detect_knowledge_tags(stem, analysis, chapter_key)

        title_short = stem.replace("\n", " ").strip()
        if len(title_short) > 120:
            title_short = title_short[:120]
        title = f"C100-{no:03d} {title_short}"

        standard_answer = analysis if analysis else ""
        analysis_text = analysis if analysis else "No analysis text in source document."

        lines.append(
            "INSERT INTO qb_question("
            "title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted"
            ")\n"
            "SELECT "
            f"'{sql_escape(title)}', 6, {difficulty}, '{sql_escape(chapter_name)}', '{sql_escape(stem)}', '{sql_escape(standard_answer)}', "
            f"1, '{sql_escape(analysis_text)}', 1, 2, @admin_id, NOW(3), NOW(3), 0\n"
            "FROM DUAL\n"
            f"WHERE NOT EXISTS (SELECT 1 FROM qb_question WHERE title='{sql_escape(title)}');"
        )

        lines.append(
            "UPDATE qb_question SET "
            f"question_type=6, difficulty={difficulty}, chapter='{sql_escape(chapter_name)}', "
            f"stem='{sql_escape(stem)}', standard_answer='{sql_escape(standard_answer)}', "
            f"answer_format=1, analysis_text='{sql_escape(analysis_text)}', analysis_source=1, status=2, "
            "created_by=COALESCE(@admin_id, created_by), updated_at=NOW(3), is_deleted=0 "
            f"WHERE title='{sql_escape(title)}';"
        )

        imported_rows.append(
            {
                "title": title,
                "chapter_tag": chapter_name,
                "difficulty_tag": f"难度{difficulty}",
                "knowledge_tags": knowledge_tags,
            }
        )

    lines.append("-- generic tag tree")
    for stmt in tag_upsert_sql("章节", "ROOT_CHAPTER", "NULL", 1, 2, 10):
        lines.append(stmt)
    for stmt in tag_upsert_sql("知识点", "ROOT_KNOWLEDGE", "NULL", 1, 1, 20):
        lines.append(stmt)
    for stmt in tag_upsert_sql("难度", "ROOT_DIFFICULTY", "NULL", 1, 1, 30):
        lines.append(stmt)

    lines.append("SET @tag_root_chapter := (SELECT id FROM qb_tag WHERE tag_name='章节' LIMIT 1);")
    lines.append("SET @tag_root_knowledge := (SELECT id FROM qb_tag WHERE tag_name='知识点' LIMIT 1);")
    lines.append("SET @tag_root_difficulty := (SELECT id FROM qb_tag WHERE tag_name='难度' LIMIT 1);")

    chapter_tags = [
        "基础语法",
        "字符串处理",
        "数组与矩阵",
        "函数与递归",
        "指针基础",
        "数据结构基础",
        "文件输入输出",
    ]
    for idx, name in enumerate(chapter_tags, start=1):
        for stmt in tag_upsert_sql(name, f"CH_{idx:02d}", "@tag_root_chapter", 2, 2, idx):
            lines.append(stmt)

    knowledge_tags = [
        "数组",
        "字符串",
        "哈希",
        "队列",
        "栈",
        "链表",
        "指针",
        "递归",
        "排序",
        "查找",
        "函数",
        "文件",
        "数学计算",
        "循环与分支",
    ]
    for idx, name in enumerate(knowledge_tags, start=1):
        for stmt in tag_upsert_sql(name, f"KN_{idx:02d}", "@tag_root_knowledge", 2, 1, idx):
            lines.append(stmt)

    for idx in range(1, 6):
        for stmt in tag_upsert_sql(f"难度{idx}", f"DF_{idx}", "@tag_root_difficulty", 2, 1, idx):
            lines.append(stmt)

    lines.append("-- question-tag relations")
    for row in imported_rows:
        lines.append(relation_sql(row["title"], row["chapter_tag"]))
        lines.append(relation_sql(row["title"], row["difficulty_tag"]))
        for knowledge_tag in row["knowledge_tags"]:
            lines.append(relation_sql(row["title"], knowledge_tag))

    lines.append("COMMIT;")
    return "\n".join(lines) + "\n"


def default_docx(root: Path) -> Path:
    matches = sorted(root.rglob("*.docx"))
    if not matches:
        raise FileNotFoundError("No .docx file found under repository")
    return matches[0]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Generate SQL import for C100 questions")
    parser.add_argument("--docx", type=Path, default=None)
    parser.add_argument("--output", type=Path, default=Path("generated/import_c100_questions.sql"))
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    repo_root = Path(__file__).resolve().parents[1]
    docx_path = args.docx if args.docx else default_docx(repo_root)
    if not docx_path.exists():
        raise FileNotFoundError(f"docx not found: {docx_path}")

    raw_lines = load_docx_lines(docx_path)
    questions = parse_questions(raw_lines)
    if len(questions) < MAX_QUESTIONS:
        raise RuntimeError(f"parsed only {len(questions)} questions; expected {MAX_QUESTIONS}")
    questions = questions[:MAX_QUESTIONS]

    sql = build_sql(questions)
    output_path = args.output
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(sql, encoding="utf-8")

    diff_stats = {k: 0 for k in range(1, 6)}
    for q in questions:
        diff_stats[estimate_difficulty(int(q["no"]), q["stem"], q["analysis"])] += 1

    print(f"Source docx : {docx_path}")
    print(f"Output sql  : {output_path}")
    print(f"Question cnt: {len(questions)}")
    print("Difficulty distribution:", ", ".join(f"{k}:{v}" for k, v in diff_stats.items()))


if __name__ == "__main__":
    main()
