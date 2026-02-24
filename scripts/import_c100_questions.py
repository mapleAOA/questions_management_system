#!/usr/bin/env python3
"""
Parse the local C 100-questions docx, estimate difficulty, and generate MySQL import SQL.
Also fills tag master data and qb_question_tag relations.
"""

from __future__ import annotations

import argparse
import re
import zipfile
from pathlib import Path
from typing import Dict, List

QUESTION_HEADER_RE = re.compile(r"^(\d{1,3})\.(.+)$")
MAX_QUESTIONS = 100

CHAPTER_TO_TAG = {
    "Basic": "C100-基础",
    "String": "C100-字符串",
    "Array": "C100-数组",
    "Function": "C100-函数与递归",
    "Pointer": "C100-指针",
    "DataStructure": "C100-数据结构",
    "FileIO": "C100-文件",
}


def load_docx_lines(docx_path: Path) -> List[str]:
    with zipfile.ZipFile(docx_path) as zf:
        xml = zf.read("word/document.xml").decode("utf-8")

    text = re.sub(r"<w:tab[^>]*/>", "\t", xml)
    text = re.sub(r"</w:p>", "\n", text)
    text = re.sub(r"<[^>]+>", "", text)
    text = (
        text.replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("\u3000", " ")
    )
    return [line.strip() for line in text.splitlines() if line.strip()]


def parse_questions(lines: List[str]) -> List[Dict[str, str]]:
    questions: List[Dict[str, str]] = []
    current_no = None
    current_title = ""
    body: List[str] = []

    def flush_current() -> None:
        nonlocal current_no, current_title, body
        if current_no is None:
            return
        questions.append(
            {
                "no": current_no,
                "stem": current_title.strip(),
                "analysis": "\n".join(body).strip() or "",
            }
        )

    for line in lines:
        m = QUESTION_HEADER_RE.match(line)
        if m:
            number = int(m.group(1))
            if 1 <= number <= MAX_QUESTIONS:
                flush_current()
                current_no = number
                current_title = m.group(2).strip()
                body = []
                continue
        if current_no is not None:
            body.append(line)

    flush_current()

    by_no = {}
    for item in questions:
        no = int(item["no"])
        if no not in by_no:
            by_no[no] = item
    return [by_no[k] for k in sorted(by_no.keys()) if 1 <= k <= MAX_QUESTIONS]


def pick_chapter(text: str) -> str:
    if any(k in text for k in ["数组", "矩阵"]):
        return "Array"
    if any(k in text for k in ["字符串", "字符"]):
        return "String"
    if any(k in text for k in ["指针", "地址"]):
        return "Pointer"
    if any(k in text for k in ["函数", "递归"]):
        return "Function"
    if any(k in text for k in ["结构体", "链表"]):
        return "DataStructure"
    if any(k in text for k in ["文件", "磁盘"]):
        return "FileIO"
    return "Basic"


def estimate_difficulty(no: int, stem: str, analysis: str) -> int:
    text = f"{stem}\n{analysis}"

    if no <= 15:
        diff = 1
    elif no <= 50:
        diff = 2
    elif no <= 80:
        diff = 3
    else:
        diff = 4

    medium_keywords = ["数组", "字符串", "排序", "查找", "矩阵", "函数"]
    hard_keywords = ["递归", "指针", "结构体", "链表", "文件", "约瑟夫", "汉诺塔"]
    easy_keywords = ["打印", "求和", "阶乘", "水仙花", "最大公约数", "最小公倍数"]

    if any(k in text for k in medium_keywords):
        diff += 1
    if any(k in text for k in hard_keywords):
        diff += 1
    if any(k in text for k in easy_keywords):
        diff -= 1

    return max(1, min(5, diff))


def sql_escape(text: str) -> str:
    return text.replace("\\", "\\\\").replace("'", "''")


def ensure_tag_sql(tag_name: str, tag_code: str, parent_expr: str, tag_level: int, tag_type: int, sort_order: int) -> str:
    return (
        "INSERT INTO qb_tag(tag_name, tag_code, parent_id, tag_level, tag_type, sort_order, created_at, updated_at, is_deleted)\n"
        f"SELECT '{sql_escape(tag_name)}', '{sql_escape(tag_code)}', {parent_expr}, {tag_level}, {tag_type}, {sort_order}, NOW(3), NOW(3), 0\n"
        "FROM DUAL\n"
        f"WHERE NOT EXISTS (SELECT 1 FROM qb_tag WHERE tag_name='{sql_escape(tag_name)}' AND is_deleted=0);"
    )


def build_sql(questions: List[Dict[str, str]]) -> str:
    lines: List[str] = []
    lines.append("SET NAMES utf8mb4;")
    lines.append("START TRANSACTION;")
    lines.append("DELETE qt FROM qb_question_tag qt JOIN qb_question q ON q.id=qt.question_id WHERE q.title LIKE 'C100-%';")
    lines.append("DELETE FROM qb_question WHERE title LIKE 'C100-%';")

    for item in questions:
        no = int(item["no"])
        stem = item["stem"].strip()
        analysis = item["analysis"].strip()
        chapter = pick_chapter(stem + "\n" + analysis)
        difficulty = estimate_difficulty(no, stem, analysis)

        title_short = stem.replace("\n", " ").strip()
        if len(title_short) > 120:
            title_short = title_short[:120]
        title = f"C100-{no:03d} {title_short}"

        standard_answer = analysis if analysis else ""
        analysis_text = analysis if analysis else "No analysis text in source document."

        lines.append(
            "INSERT INTO qb_question("
            "title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted"
            ") VALUES ("
            f"'{sql_escape(title)}', "
            "6, "
            f"{difficulty}, "
            f"'{sql_escape(chapter)}', "
            f"'{sql_escape(stem)}', "
            f"'{sql_escape(standard_answer)}', "
            "1, "
            f"'{sql_escape(analysis_text)}', "
            "1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0"
            ");"
        )

    lines.append("-- tag master data")
    lines.append(ensure_tag_sql("题库-C100", "C100_BANK", "NULL", 1, 3, 100))
    lines.append(ensure_tag_sql("章节-C100", "C100_CHAPTER", "NULL", 1, 2, 110))
    lines.append(ensure_tag_sql("难度-C100", "C100_DIFF", "NULL", 1, 1, 120))

    for idx, (chapter, tag_name) in enumerate(CHAPTER_TO_TAG.items(), start=1):
        lines.append(
            ensure_tag_sql(
                tag_name,
                f"C100_CH_{chapter.upper()}",
                "(SELECT id FROM qb_tag WHERE tag_name='章节-C100' AND is_deleted=0 LIMIT 1)",
                2,
                2,
                idx,
            )
        )

    for i in range(1, 6):
        lines.append(
            ensure_tag_sql(
                f"C100-难度{i}",
                f"C100_DIFF_{i}",
                "(SELECT id FROM qb_tag WHERE tag_name='难度-C100' AND is_deleted=0 LIMIT 1)",
                2,
                1,
                i,
            )
        )

    lines.append("-- relation: all C100 questions -> 题库-C100")
    lines.append(
        "INSERT INTO qb_question_tag(question_id, tag_id, created_at)\n"
        "SELECT q.id, t.id, NOW(3)\n"
        "FROM qb_question q\n"
        "JOIN qb_tag t ON t.tag_name='题库-C100' AND t.is_deleted=0\n"
        "LEFT JOIN qb_question_tag qt ON qt.question_id=q.id AND qt.tag_id=t.id\n"
        "WHERE q.title LIKE 'C100-%' AND q.is_deleted=0 AND qt.question_id IS NULL;"
    )

    lines.append("-- relation: chapter tag")
    lines.append(
        "INSERT INTO qb_question_tag(question_id, tag_id, created_at)\n"
        "SELECT q.id, t.id, NOW(3)\n"
        "FROM qb_question q\n"
        "JOIN qb_tag t ON t.tag_name = (\n"
        "  CASE q.chapter\n"
        "    WHEN 'Basic' THEN 'C100-基础'\n"
        "    WHEN 'String' THEN 'C100-字符串'\n"
        "    WHEN 'Array' THEN 'C100-数组'\n"
        "    WHEN 'Function' THEN 'C100-函数与递归'\n"
        "    WHEN 'Pointer' THEN 'C100-指针'\n"
        "    WHEN 'DataStructure' THEN 'C100-数据结构'\n"
        "    WHEN 'FileIO' THEN 'C100-文件'\n"
        "    ELSE 'C100-基础'\n"
        "  END\n"
        ") AND t.is_deleted=0\n"
        "LEFT JOIN qb_question_tag qt ON qt.question_id=q.id AND qt.tag_id=t.id\n"
        "WHERE q.title LIKE 'C100-%' AND q.is_deleted=0 AND qt.question_id IS NULL;"
    )

    lines.append("-- relation: difficulty tag")
    lines.append(
        "INSERT INTO qb_question_tag(question_id, tag_id, created_at)\n"
        "SELECT q.id, t.id, NOW(3)\n"
        "FROM qb_question q\n"
        "JOIN qb_tag t ON t.tag_name = CONCAT('C100-难度', q.difficulty) AND t.is_deleted=0\n"
        "LEFT JOIN qb_question_tag qt ON qt.question_id=q.id AND qt.tag_id=t.id\n"
        "WHERE q.title LIKE 'C100-%' AND q.is_deleted=0 AND qt.question_id IS NULL;"
    )

    lines.append("COMMIT;")
    return "\n".join(lines) + "\n"


def default_docx(root: Path) -> Path:
    matches = sorted(root.rglob("*.docx"))
    if not matches:
        raise FileNotFoundError("No .docx file found under repository")
    return matches[0]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Generate SQL import for C100 questions")
    parser.add_argument(
        "--docx",
        type=Path,
        default=None,
        help="Path to source .docx file (defaults to first .docx in repo)",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=Path("generated/import_c100_questions.sql"),
        help="Output SQL file path",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    repo_root = Path(__file__).resolve().parents[1]
    docx_path = args.docx if args.docx else default_docx(repo_root)
    if not docx_path.exists():
        raise FileNotFoundError(f"docx not found: {docx_path}")

    lines = load_docx_lines(docx_path)
    questions = parse_questions(lines)

    if len(questions) < MAX_QUESTIONS:
        raise RuntimeError(f"parsed only {len(questions)} questions from {docx_path}; expected at least {MAX_QUESTIONS}")
    if len(questions) > MAX_QUESTIONS:
        questions = questions[:MAX_QUESTIONS]

    sql = build_sql(questions)
    output_path = args.output
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(sql, encoding="utf-8")

    diff_stats = {k: 0 for k in range(1, 6)}
    for q in questions:
        d = estimate_difficulty(int(q["no"]), q["stem"], q["analysis"])
        diff_stats[d] += 1

    print(f"Source docx : {docx_path}")
    print(f"Output sql  : {output_path}")
    print(f"Question cnt: {len(questions)}")
    print("Difficulty distribution:", ", ".join(f"{k}:{v}" for k, v in diff_stats.items()))
    print("Chapter tags:", ", ".join(CHAPTER_TO_TAG.values()))


if __name__ == "__main__":
    main()