SET NAMES utf8mb4;
START TRANSACTION;
DELETE qt FROM qb_question_tag qt JOIN qb_question q ON q.id=qt.question_id WHERE q.title LIKE 'C100-%';
DELETE FROM qb_question WHERE title LIKE 'C100-%';
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-001 输入两个正整数，m和n，求其最大公约数和最小公倍数。', 6, 1, 'Basic', '输入两个正整数，m和n，求其最大公约数和最小公倍数。', '#include
void main()
{
int hcf(int,int);
int lcd(int,int,int);
int u,v,h,l;
printf("Please ｉｎｐｕｔ two numbers:\\n");
scanf("%d,%d",&u,&v);
h=hcf(u,v);
printf("H.C.F=%d\\n",h);
l=lcd(u,v,h);
printf("L.C.D=%d\\n",l);
}
int hcf(int u,int v)
{
int t,r;
if(v>u)
{t=u;u=v;v=t;}
while((r=u%v)!=0)
{u=v;v=r;}
return(v);
}
int lcd(int u,int v,int h)
{
return(u*v/h);
}', 1, '#include
void main()
{
int hcf(int,int);
int lcd(int,int,int);
int u,v,h,l;
printf("Please ｉｎｐｕｔ two numbers:\\n");
scanf("%d,%d",&u,&v);
h=hcf(u,v);
printf("H.C.F=%d\\n",h);
l=lcd(u,v,h);
printf("L.C.D=%d\\n",l);
}
int hcf(int u,int v)
{
int t,r;
if(v>u)
{t=u;u=v;v=t;}
while((r=u%v)!=0)
{u=v;v=r;}
return(v);
}
int lcd(int u,int v,int h)
{
return(u*v/h);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-002 输入一行字符，分别统计出其中字母、空格、数字和其他字符的个数。', 6, 1, 'String', '输入一行字符，分别统计出其中字母、空格、数字和其他字符的个数。', '#include
int letter,digit,space,others;
void main()
{
void count(char[]);
char text[80];
printf("Please ｉｎｐｕｔ string:\\n");
gets(text);
printf("string:\\n");
puts(text);
letter=0;
digit=0;
space=0;
others=0;
count(text);
printf("letter:%d,digit:%d,space:%d,others:%d\\n",letter,digit,space,others);
}
void count(char str[])
{
int i;
for(i=0;str!=''\\0'';i++)
if((str>=''a''&&str<=''z'')||(str>=''A''&&str<=''Z''))
letter++;
else if(str>=''0''&&str<=''9'')
digit++;
else if(str==32)
space++;
else
others++;
}', 1, '#include
int letter,digit,space,others;
void main()
{
void count(char[]);
char text[80];
printf("Please ｉｎｐｕｔ string:\\n");
gets(text);
printf("string:\\n");
puts(text);
letter=0;
digit=0;
space=0;
others=0;
count(text);
printf("letter:%d,digit:%d,space:%d,others:%d\\n",letter,digit,space,others);
}
void count(char str[])
{
int i;
for(i=0;str!=''\\0'';i++)
if((str>=''a''&&str<=''z'')||(str>=''A''&&str<=''Z''))
letter++;
else if(str>=''0''&&str<=''9'')
digit++;
else if(str==32)
space++;
else
others++;
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-003 输入一个正整数求出它是几位数；输出原数和位数。', 6, 1, 'Basic', '输入一个正整数求出它是几位数；输出原数和位数。', '#include
int digit;
void main()
{
void count(char[]);
char text[80];
printf("Please ｉｎｐｕｔ numbers:\\n");
gets(text);
printf("Numbers:\\n");
puts(text);
digit=0;
count(text);
printf("digit:%d\\n",digit);
}
void count(char str[])
{
int i;
for(i=0;str!=''\\0'';i++)
if(str>=''0''&&str<=''9'')
digit++;
}', 1, '#include
int digit;
void main()
{
void count(char[]);
char text[80];
printf("Please ｉｎｐｕｔ numbers:\\n");
gets(text);
printf("Numbers:\\n");
puts(text);
digit=0;
count(text);
printf("digit:%d\\n",digit);
}
void count(char str[])
{
int i;
for(i=0;str!=''\\0'';i++)
if(str>=''0''&&str<=''9'')
digit++;
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-004 输入一个正整数，输出原数并逆序打印出各位数字。', 6, 1, 'Basic', '输入一个正整数，输出原数并逆序打印出各位数字。', '#include
void invertLongInt(long);
void main()
{
unsigned long iNumber;
printf("Please ｉｎｐｕｔ a number:\\n");
scanf("%ld",&iNumber);
printf("The ｉｎｐｕｔ number is:%ld\\n",iNumber);
printf("The inverse number is:");
invertLongInt(iNumber);
}
void invertLongInt(long x)
{
if(x>=0&&x<=9)
printf("%d\\n",x);
else
{
printf("%d",x%10);
invertLongInt(x/10);
}
}', 1, '#include
void invertLongInt(long);
void main()
{
unsigned long iNumber;
printf("Please ｉｎｐｕｔ a number:\\n");
scanf("%ld",&iNumber);
printf("The ｉｎｐｕｔ number is:%ld\\n",iNumber);
printf("The inverse number is:");
invertLongInt(iNumber);
}
void invertLongInt(long x)
{
if(x>=0&&x<=9)
printf("%d\\n",x);
else
{
printf("%d",x%10);
invertLongInt(x/10);
}
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-005 从键盘上输入若干学生的一门课成绩，统计并输出最高成绩和最低成绩及相应的序号，当输入负数时结束输入。', 6, 1, 'Basic', '从键盘上输入若干学生的一门课成绩，统计并输出最高成绩和最低成绩及相应的序号，当输入负数时结束输入。', '', 1, 'No analysis text in source document.', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-006 从键盘上输入若干学生的一门课成绩，计算出平均分，当输入负数时结束输入。将结果输出。', 6, 1, 'Basic', '从键盘上输入若干学生的一门课成绩，计算出平均分，当输入负数时结束输入。将结果输出。', '', 1, 'No analysis text in source document.', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-007 求1!+2!+3!+……+20!，将结果输出。', 6, 1, 'Basic', '求1!+2!+3!+……+20!，将结果输出。', '#include
void main()
{
float s=0,t=1;
int n;
for(n=1;n<=20;n++)
{
t=t*n;
s=s+t;
}
printf("1!+2!+3!+……+20!=%e\\n",s);
}', 1, '#include
void main()
{
float s=0,t=1;
int n;
for(n=1;n<=20;n++)
{
t=t*n;
s=s+t;
}
printf("1!+2!+3!+……+20!=%e\\n",s);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-008 打印以下图案： *', 6, 1, 'Basic', '打印以下图案： *', '***
*****
*******
#include
void main()
{
int i,j;
printf("The picture is:\\n");
static char picture[4][7]={{'' '','' '','' '',''*''},
{'' '','' '',''*'',''*'',''*''},{'' '','' *'',''*'',''*'',''*'',''*''},{''*'',''*'',''*'',''*'',''*'',''*'',''*''}};
for(i=0;i<=3;i++)
{
for(j=0;j<=6;j++)
printf("%c",picture[j]);
printf("\\n");
}
}', 1, '***
*****
*******
#include
void main()
{
int i,j;
printf("The picture is:\\n");
static char picture[4][7]={{'' '','' '','' '',''*''},
{'' '','' '',''*'',''*'',''*''},{'' '','' *'',''*'',''*'',''*'',''*''},{''*'',''*'',''*'',''*'',''*'',''*'',''*''}};
for(i=0;i<=3;i++)
{
for(j=0;j<=6;j++)
printf("%c",picture[j]);
printf("\\n");
}
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-009 打印以下图案：', 6, 1, 'Basic', '打印以下图案：', '*
**
***
****
#include
void main()
{
int i,j;
printf("The picture is:\\n");
char picture[4][4]={{''*''},
{''*'',''*''},{'' *'',''*'',''*''},{''*'',''*'',''*'',''*''}};
for(i=0;i<=3;i++)
{
for(j=0;j<=3;j++)
printf("%c",picture[j]);
printf("\\n");
}
}', 1, '*
**
***
****
#include
void main()
{
int i,j;
printf("The picture is:\\n");
char picture[4][4]={{''*''},
{''*'',''*''},{'' *'',''*'',''*''},{''*'',''*'',''*'',''*''}};
for(i=0;i<=3;i++)
{
for(j=0;j<=3;j++)
printf("%c",picture[j]);
printf("\\n");
}
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-010 求下列试子的值：1-1/2+1/3-1/4+……+1/99-1/100，将结果输出。', 6, 1, 'Basic', '求下列试子的值：1-1/2+1/3-1/4+……+1/99-1/100，将结果输出。', '#include
void main()
{
float sum=1.0,t,s=1;
int i;
for(i=1;i<=100;i++)
{
t=s/i;
sum=sum+t;
s=-s;
}
printf("1-1/2+1/3-1/4+……+1/99-1/100=%5.4f\\n",sum);
}', 1, '#include
void main()
{
float sum=1.0,t,s=1;
int i;
for(i=1;i<=100;i++)
{
t=s/i;
sum=sum+t;
s=-s;
}
printf("1-1/2+1/3-1/4+……+1/99-1/100=%5.4f\\n",sum);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-011 打印出100～999之间的所有水仙花数。', 6, 1, 'Basic', '打印出100～999之间的所有水仙花数。', '#include
void main()
{
int i,j,k,n;
printf("100～999之间的所有水仙花数 are:\\n");
for(n=100;n<1000;n++)
{
i=n/100;
j=n/10-i*10;
k=n%10;
if(n==i*i*i+j*j*j+k*k*k)
printf("%d ",n);
}
printf("\\n");
}', 1, '#include
void main()
{
int i,j,k,n;
printf("100～999之间的所有水仙花数 are:\\n");
for(n=100;n<1000;n++)
{
i=n/100;
j=n/10-i*10;
k=n%10;
if(n==i*i*i+j*j*j+k*k*k)
printf("%d ",n);
}
printf("\\n");
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-012 求Sn=a+aa+aaa+…+aa…a之值，n,a由键盘输入。', 6, 1, 'Basic', '求Sn=a+aa+aaa+…+aa…a之值，n,a由键盘输入。', '#include
void main()
{
int a,n,i=1,sn=0,tn=0;
printf("a,n=:");
scanf("%d,%d",&a,&n);
while(i<=n)
{
tn=tn+a;
sn=sn+tn;
a=a*10;
++i;
}
printf("a+aa+aaa+…+aa…a=%d\\n",sn);
}', 1, '#include
void main()
{
int a,n,i=1,sn=0,tn=0;
printf("a,n=:");
scanf("%d,%d",&a,&n);
while(i<=n)
{
tn=tn+a;
sn=sn+tn;
a=a*10;
++i;
}
printf("a+aa+aaa+…+aa…a=%d\\n",sn);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-013 打印以下图案：', 6, 1, 'Basic', '打印以下图案：', '*******
*******
*******
*******
#include
void main()
{
char a[7]={''*'',''*'',''*'',''*'',''*'',''*'',''*''};
int i,j,k;
char space='' '';
printf("The picture is:");
for(i=0;i<4;i++)
{
printf("\\n");
printf(" ");
for(j=1;j<=i;j++)
printf("%c",space);
for(k=0;k<7;k++)
printf("%c",a[k]);
}
printf("\\n");
}', 1, '*******
*******
*******
*******
#include
void main()
{
char a[7]={''*'',''*'',''*'',''*'',''*'',''*'',''*''};
int i,j,k;
char space='' '';
printf("The picture is:");
for(i=0;i<4;i++)
{
printf("\\n");
printf(" ");
for(j=1;j<=i;j++)
printf("%c",space);
for(k=0;k<7;k++)
printf("%c",a[k]);
}
printf("\\n");
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-014 打印以下图案：', 6, 1, 'Basic', '打印以下图案：', '1
121
12321
1234321', 1, '1
121
12321
1234321', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-015 打印以下图案：', 6, 1, 'Basic', '打印以下图案：', '1234321
12321
121
1', 1, '1234321
12321
121
1', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-016 编写一个统计学生成绩程序，完成以下功能：输入4个学生的2门课成绩；求出全班的总平均分，将结果输出。', 6, 2, 'Basic', '编写一个统计学生成绩程序，完成以下功能：输入4个学生的2门课成绩；求出全班的总平均分，将结果输出。', '#include
#define N 4
struct student
{
char num[3];
char name[4];
float score[2];
float avr;
}
stu[N];
void main()
{
int i,j;
float sum,average;
for(i=0;i
{
printf("ｉｎｐｕｔ scores of student%d:\\n",i+1);
printf("name:");
scanf("%s",stu.name);
for(j=0;j<2;j++)
{
printf("score %d:",j+1);
scanf("%f",&stu.score[j]);
}
}
average=0;
for(i=0;i
{
sum=0;
for(j=0;j<2;j++)
sum+=stu.score[j];
stu.avr=sum/2;
average+=stu.avr;
}
average/=N;
printf(" Name score1 score2 average\\n");
for(i=0;i
{
printf("%5s%10s",stu.num,stu.name);
for(j=0;j<2;j++)
printf("%9.2f",stu.score[j]);
printf(" %8.2f\\n",stu.avr);
}
printf("average=%5.2f\\n",average);
}', 1, '#include
#define N 4
struct student
{
char num[3];
char name[4];
float score[2];
float avr;
}
stu[N];
void main()
{
int i,j;
float sum,average;
for(i=0;i
{
printf("ｉｎｐｕｔ scores of student%d:\\n",i+1);
printf("name:");
scanf("%s",stu.name);
for(j=0;j<2;j++)
{
printf("score %d:",j+1);
scanf("%f",&stu.score[j]);
}
}
average=0;
for(i=0;i
{
sum=0;
for(j=0;j<2;j++)
sum+=stu.score[j];
stu.avr=sum/2;
average+=stu.avr;
}
average/=N;
printf(" Name score1 score2 average\\n");
for(i=0;i
{
printf("%5s%10s",stu.num,stu.name);
for(j=0;j<2;j++)
printf("%9.2f",stu.score[j]);
printf(" %8.2f\\n",stu.avr);
}
printf("average=%5.2f\\n",average);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-017 打印以下图案：', 6, 1, 'Basic', '打印以下图案：', '*****
*****
*****
*****
*****
#include
void main()
{
char a[5]={''*'',''*'',''*'',''*'',''*''};
int i,j,k;
char space='' '';
printf("The picture is:");
for(i=0;i<5;i++)
{
printf("\\n");
printf(" ");
for(j=1;j<=i;j++)
printf("%c",space);
for(k=0;k<5;k++)
printf("%c",a[k]);
}
printf("\\n");
}', 1, '*****
*****
*****
*****
*****
#include
void main()
{
char a[5]={''*'',''*'',''*'',''*'',''*''};
int i,j,k;
char space='' '';
printf("The picture is:");
for(i=0;i<5;i++)
{
printf("\\n");
printf(" ");
for(j=1;j<=i;j++)
printf("%c",space);
for(k=0;k<5;k++)
printf("%c",a[k]);
}
printf("\\n");
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-018 给出年、月、日，计算该日是该年的第几天。', 6, 2, 'Basic', '给出年、月、日，计算该日是该年的第几天。', '#include
void main()
{
int sum_day(int,int);
int leap(int year);
int year,month,day,days;
printf("ｉｎｐｕｔ date(year,month,day):");
scanf("%d,%d,%d",&year,&month,&day);
printf("%d/%d/%d",year,month,day);
days=sum_day(month,day);
if(leap(year)&&month>=3)
days=days+1;
printf("is the %dth day in this year.\\n",days);
}
int sum_day(int month,int day)
{
int day_tab[13]={0,31,28,31,30,31,30,31,31,30,31,30,31};
int i;
for(i=1;i
day+=day_tab;
return(day);
}
int leap(int year)
{
int leap;
leap=year%4==0&&year%100!=0||year%400==0;
return(leap);
}', 1, '#include
void main()
{
int sum_day(int,int);
int leap(int year);
int year,month,day,days;
printf("ｉｎｐｕｔ date(year,month,day):");
scanf("%d,%d,%d",&year,&month,&day);
printf("%d/%d/%d",year,month,day);
days=sum_day(month,day);
if(leap(year)&&month>=3)
days=days+1;
printf("is the %dth day in this year.\\n",days);
}
int sum_day(int month,int day)
{
int day_tab[13]={0,31,28,31,30,31,30,31,31,30,31,30,31};
int i;
for(i=1;i
day+=day_tab;
return(day);
}
int leap(int year)
{
int leap;
leap=year%4==0&&year%100!=0||year%400==0;
return(leap);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-019 求一个3*3的整型矩阵对角线元素之和。将原矩阵和求出的和输出。', 6, 3, 'Array', '求一个3*3的整型矩阵对角线元素之和。将原矩阵和求出的和输出。', '#include
void main()
{
float a[3][3],sum=0;
int i,j;
printf("请输入元素:\\n");
for(i=0;i<3;i++)
for(j=0;j<3;j++)
scanf("%f",&a[j]);
for(i=0;i<3;i++)
sum=sum+a;
printf("对角线之和是：%6.2f\\n",sum);
for(i=0;i<=3;i++)
{
for(j=0;j<=3;j++)
printf("%5.2f",a[j]);
printf("\\n");
}
}', 1, '#include
void main()
{
float a[3][3],sum=0;
int i,j;
printf("请输入元素:\\n");
for(i=0;i<3;i++)
for(j=0;j<3;j++)
scanf("%f",&a[j]);
for(i=0;i<3;i++)
sum=sum+a;
printf("对角线之和是：%6.2f\\n",sum);
for(i=0;i<=3;i++)
{
for(j=0;j<=3;j++)
printf("%5.2f",a[j]);
printf("\\n");
}
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-020 求一个4*3的矩阵各行元素的平均值；将原矩阵和求出的平均值全部输出。', 6, 3, 'Array', '求一个4*3的矩阵各行元素的平均值；将原矩阵和求出的平均值全部输出。', '#include
void main()
{
int a[4][3],s[4],i,j,k;
for(i=0;i<4;i++)
for(j=0;j<3;j++)
scanf("%d",*(a+i)+j);
for(i=0;i<4;i++)
{
k=0;
for(j=0;j<3;j++)
k+=a[j];
printf("第%d行的平均值是%d",i+1,k);
printf("\\n");
}
}', 1, '#include
void main()
{
int a[4][3],s[4],i,j,k;
for(i=0;i<4;i++)
for(j=0;j<3;j++)
scanf("%d",*(a+i)+j);
for(i=0;i<4;i++)
{
k=0;
for(j=0;j<3;j++)
k+=a[j];
printf("第%d行的平均值是%d",i+1,k);
printf("\\n");
}
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-021 求一个3*4的矩阵各列元素的平均值；将原矩阵和求出的平均值全部输出。', 6, 3, 'Array', '求一个3*4的矩阵各列元素的平均值；将原矩阵和求出的平均值全部输出。', '#include
void main()
{
int a[4][3],s[4],i,j,k;
for(i=0;i<4;i++)
for(j=0;j<3;j++)
scanf("%d",*(a+i)+j);
for(i=0;i<4;i++)
{
k=0;
for(j=0;j<3;j++)
k+=a[j];
printf("第%d行的平均值是%d",i+1,k);
printf("\\n");
}
}', 1, '#include
void main()
{
int a[4][3],s[4],i,j,k;
for(i=0;i<4;i++)
for(j=0;j<3;j++)
scanf("%d",*(a+i)+j);
for(i=0;i<4;i++)
{
k=0;
for(j=0;j<3;j++)
k+=a[j];
printf("第%d行的平均值是%d",i+1,k);
printf("\\n");
}
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-022 求一个3*5的矩阵各列元素的最大值，将原矩阵和求出的最大值全部输出。', 6, 3, 'Array', '求一个3*5的矩阵各列元素的最大值，将原矩阵和求出的最大值全部输出。', '#include
void main()
{
int a[3][5],s[3],i,j,k;
for(i=0;i<3;i++)
for(j=0;j<5;j++)
scanf("%d",*(a+i)+j);
for(i=0;i<3;i++)
{ *(s+i)= *(*(a+j));
for(j=1;j<5;j++)
if(*(s+i) < *(*(a+i)+j))
*(s+i)= *(*(a+i)+j);
}
for(i=0;i<3;i++)
{
printf("Line=%d Max=%d",j,s[j] );
printf("\\n");
}
}', 1, '#include
void main()
{
int a[3][5],s[3],i,j,k;
for(i=0;i<3;i++)
for(j=0;j<5;j++)
scanf("%d",*(a+i)+j);
for(i=0;i<3;i++)
{ *(s+i)= *(*(a+j));
for(j=1;j<5;j++)
if(*(s+i) < *(*(a+i)+j))
*(s+i)= *(*(a+i)+j);
}
for(i=0;i<3;i++)
{
printf("Line=%d Max=%d",j,s[j] );
printf("\\n");
}
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-023 求一个4*3的矩阵各行元素的最大值，将原矩阵和求出的最大值全部输出。', 6, 3, 'Array', '求一个4*3的矩阵各行元素的最大值，将原矩阵和求出的最大值全部输出。', '#include
void main()
{
int a[4][3],s[4],i,j,k;
for(i=0;i<4;i++)
for(j=0;j<3;j++)
scanf("%d",*(a+i)+j);
for(i=0;i<4;i++)
{ *(s+i)= *(*(a+i));
for(j=1;j<3;j++)
if(*(s+i) < *(*(a+i)+j))
*(s+i)= *(*(a+i)+j);
}
for(i=0;i<4;i++)
{
printf("Row=%d Max=%d",i,s );
printf("\\n");
}
}', 1, '#include
void main()
{
int a[4][3],s[4],i,j,k;
for(i=0;i<4;i++)
for(j=0;j<3;j++)
scanf("%d",*(a+i)+j);
for(i=0;i<4;i++)
{ *(s+i)= *(*(a+i));
for(j=1;j<3;j++)
if(*(s+i) < *(*(a+i)+j))
*(s+i)= *(*(a+i)+j);
}
for(i=0;i<4;i++)
{
printf("Row=%d Max=%d",i,s );
printf("\\n");
}
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-024 求一个M*N的矩阵中元素的最大值，将原矩阵和求出的最大值全部输出。', 6, 3, 'Array', '求一个M*N的矩阵中元素的最大值，将原矩阵和求出的最大值全部输出。', '#include
#define N 10
#define M 10
void main()
{
int a[M][N],i,j,k[M],max,m,n;
scanf("%d %d",&m,&n);
for(i=0;i
for(j=0;j
scanf("%d",*(a+i)+j);
for(i=0;i
{
for(j=0;j
{
if(a[j]<=a[j+1])
max=a[j+1];
else
max=a[j];
}
k=max;
}
for(i=0;i
{
if(k<=k[i+1])
max=k[i+1];
else
max=k;
}
printf("%d",max);
}', 1, '#include
#define N 10
#define M 10
void main()
{
int a[M][N],i,j,k[M],max,m,n;
scanf("%d %d",&m,&n);
for(i=0;i
for(j=0;j
scanf("%d",*(a+i)+j);
for(i=0;i
{
for(j=0;j
{
if(a[j]<=a[j+1])
max=a[j+1];
else
max=a[j];
}
k=max;
}
for(i=0;i
{
if(k<=k[i+1])
max=k[i+1];
else
max=k;
}
printf("%d",max);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-025 判断一个N*N的矩阵是否为对称矩阵，将原矩阵输出，判断结果输出。', 6, 3, 'Array', '判断一个N*N的矩阵是否为对称矩阵，将原矩阵输出，判断结果输出。', '#include
#define N 10
void main()
{
int a[N][N],i,j,k,n;
scanf("%d",&n);
for(i=0;i
for(j=0;j
scanf("%d",&a[j]);
for(i=0;i
{
for(j=i+1;j
{
if(a[j]==a[j])
k=1;
else
k=0;
}
}
if(k=0)
printf("bushi");
else
printf("shi\\n");
for(i=0;i
for(j=0;j
printf("%d",a[j]);
}', 1, '#include
#define N 10
void main()
{
int a[N][N],i,j,k,n;
scanf("%d",&n);
for(i=0;i
for(j=0;j
scanf("%d",&a[j]);
for(i=0;i
{
for(j=i+1;j
{
if(a[j]==a[j])
k=1;
else
k=0;
}
}
if(k=0)
printf("bushi");
else
printf("shi\\n");
for(i=0;i
for(j=0;j
printf("%d",a[j]);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-026 有一篇文章，有三行文字，每行有80个字符。要求统计出其中英文大写字母、消协字母、数字、空格以及其他字符的个数。', 6, 2, 'String', '有一篇文章，有三行文字，每行有80个字符。要求统计出其中英文大写字母、消协字母、数字、空格以及其他字符的个数。', '#include
void main()
{
int i,j,big=0,sma=0,num=0,spa=0,oth=0;
char text[3][80];
for(i=0;i<3;i++)
{
printf("请输入行%d:\\n",i+1);
gets(text);
for(j=0;j<80&&text[j]!=''\\0'';j++)
{
if(text[j]>=''A''&&text[j]<=''Z'')
big++;
else if(text[j]>=''a''&&text[j]<=''z'')
sma++;
else if(text[j]>=''0''&&text[j]<=''9'')
num++;
else if(text[j]=='' '')
spa++;
else
oth++;
}
}
printf("大写字母:%d\\n",big);
printf("小写字母:%d\\n",sma);
printf("数字:%d\\n",num);
printf("空格:%d\\n",spa);
printf("其它:%d\\n",oth);
}', 1, '#include
void main()
{
int i,j,big=0,sma=0,num=0,spa=0,oth=0;
char text[3][80];
for(i=0;i<3;i++)
{
printf("请输入行%d:\\n",i+1);
gets(text);
for(j=0;j<80&&text[j]!=''\\0'';j++)
{
if(text[j]>=''A''&&text[j]<=''Z'')
big++;
else if(text[j]>=''a''&&text[j]<=''z'')
sma++;
else if(text[j]>=''0''&&text[j]<=''9'')
num++;
else if(text[j]=='' '')
spa++;
else
oth++;
}
}
printf("大写字母:%d\\n",big);
printf("小写字母:%d\\n",sma);
printf("数字:%d\\n",num);
printf("空格:%d\\n",spa);
printf("其它:%d\\n",oth);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-027 将20个整数放到一维数组中，输出该数组的最大值和最小值。', 6, 3, 'Array', '将20个整数放到一维数组中，输出该数组的最大值和最小值。', '#include
void main()
{
int i,j,min,max,a[21];
printf("请输入数据:\\n");
for(i=1;i<=20;i++)
{
printf("a[%d]=",i);
scanf("%d",&a);
}
for(i=1;i<=19;i++)
{
min=i;
for(j=2;j<=19;j++)
if(a[min]>a[j]);
a[min]=a[j];
}
for(i=1;i<=19;i++)
{
max=i;
for(j=2;j<=19;j++)
if(a[max]
a[max]=a[j];
}
printf("最大数为：%d\\n",a[max]);
printf("最小数为：%d\\n",a[min]);
}', 1, '#include
void main()
{
int i,j,min,max,a[21];
printf("请输入数据:\\n");
for(i=1;i<=20;i++)
{
printf("a[%d]=",i);
scanf("%d",&a);
}
for(i=1;i<=19;i++)
{
min=i;
for(j=2;j<=19;j++)
if(a[min]>a[j]);
a[min]=a[j];
}
for(i=1;i<=19;i++)
{
max=i;
for(j=2;j<=19;j++)
if(a[max]
a[max]=a[j];
}
printf("最大数为：%d\\n",a[max]);
printf("最小数为：%d\\n",a[min]);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-028 将15个整数放到一维数组中，输出该数组中的最大值它的下标，然后将它和数组中的最前面的元素对换。', 6, 3, 'Array', '将15个整数放到一维数组中，输出该数组中的最大值它的下标，然后将它和数组中的最前面的元素对换。', '#include
void main()
{
int i,j,min,max,a[16],m,n;
printf("请输入数据:\\n");
for(i=1;i<=15;i++)
{
printf("a[%d]=",i);
scanf("%d",&a);
}
for(i=1;i<=14;i++)
{
max=i;
for(j=2;j<=14;j++)
if(a[max]
a[max]=a[j];
m=I,n=j
}
printf("最大数下标为：%d，%d\\n",m,n);
}', 1, '#include
void main()
{
int i,j,min,max,a[16],m,n;
printf("请输入数据:\\n");
for(i=1;i<=15;i++)
{
printf("a[%d]=",i);
scanf("%d",&a);
}
for(i=1;i<=14;i++)
{
max=i;
for(j=2;j<=14;j++)
if(a[max]
a[max]=a[j];
m=I,n=j
}
printf("最大数下标为：%d，%d\\n",m,n);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-029 将字符数组str1种下标为偶数的元素赋给另一字符数组str2，并输出str1和str2。', 6, 3, 'Array', '将字符数组str1种下标为偶数的元素赋给另一字符数组str2，并输出str1和str2。', '#include
#include
#define N 10
void main()
{
int i,n;
char str1[N],str2[N];
gets(str1);
for(n=0;n
{
i=2*n;
str2[n]=str1;
}
puts(str1);
puts(str2);
}', 1, '#include
#include
#define N 10
void main()
{
int i,n;
char str1[N],str2[N];
gets(str1);
for(n=0;n
{
i=2*n;
str2[n]=str1;
}
puts(str1);
puts(str2);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-030 编写一个程序，将两个字符串连接起来，不要使用strcat函数。', 6, 3, 'String', '编写一个程序，将两个字符串连接起来，不要使用strcat函数。', '#include
void main()
{
char str1[20],str2[20];
int i=0,j=0;
printf("请输入字符串1:\\n");
scanf("%s",str1);
printf("请输入字符串2:\\n");
scanf("%s",str2);
while (str1!=''\\0'')
i++;
while (str2[j]!=''\\0'')
str1[i++]=str2[j++];
str1=''\\0'';
printf("合并之后的字符串为:%s\\n",str1);
xiang6963
2008-11-12 23:42:37', 1, '#include
void main()
{
char str1[20],str2[20];
int i=0,j=0;
printf("请输入字符串1:\\n");
scanf("%s",str1);
printf("请输入字符串2:\\n");
scanf("%s",str2);
while (str1!=''\\0'')
i++;
while (str2[j]!=''\\0'')
str1[i++]=str2[j++];
str1=''\\0'';
printf("合并之后的字符串为:%s\\n",str1);
xiang6963
2008-11-12 23:42:37', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-031 编写一个程序，将字符数组a中的全部字符复制到字符数组b中。不要使用strcpy函数。', 6, 3, 'Array', '编写一个程序，将字符数组a中的全部字符复制到字符数组b中。不要使用strcpy函数。', '#include<stdio.h>
#include<string.h>
void main()
{
char a[40],b[40];
int i;
printf("请输入a:\\n");
scanf("%s",a);
for(i=0;i<=strlen(a);i++)
b=a;
printf("b:%s\\n",b);
}', 1, '#include<stdio.h>
#include<string.h>
void main()
{
char a[40],b[40];
int i;
printf("请输入a:\\n");
scanf("%s",a);
for(i=0;i<=strlen(a);i++)
b=a;
printf("b:%s\\n",b);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-032 编写一个程序，找出3个字符串中的最大者，将它输出。', 6, 3, 'String', '编写一个程序，找出3个字符串中的最大者，将它输出。', '#include<stdio.h>
#include<string.h>
void main()
{
char string[20];
char str[3][20];
int i;
for(i=0;i<3;i++)
gets (str);
if(strcmp(str[0],str[1])>0)
strcpy(string,str[0]);
else
strcpy(string,str[1]);
if(strcmp(str[2],string)>0)
strcpy(string,str[2]);
printf("最大的字符串是:\\n%s\\n",string);
}', 1, '#include<stdio.h>
#include<string.h>
void main()
{
char string[20];
char str[3][20];
int i;
for(i=0;i<3;i++)
gets (str);
if(strcmp(str[0],str[1])>0)
strcpy(string,str[0]);
else
strcpy(string,str[1]);
if(strcmp(str[2],string)>0)
strcpy(string,str[2]);
printf("最大的字符串是:\\n%s\\n",string);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-033 编写程序，输入任意一个1～7之间的整数，将他们转换成对应的英文单词。例如：1转换成Monday,7转换成Sunday。', 6, 2, 'Basic', '编写程序，输入任意一个1～7之间的整数，将他们转换成对应的英文单词。例如：1转换成Monday,7转换成Sunday。', '#include<stdio.h>
void main()
{
int a;
printf("输入一个整数：\\n");
scanf("%d",&a);
if(a==1) printf("Monday\\n");
else if(a==2) printf("Tuesday\\n");
else if(a==3) printf("Wendesday\\n");
else if(a==4) printf("Thursday\\n");
else if(a==5) printf("Friday\\n");
else if(a==6) printf("Saturday\\n");
else if(a==7) printf("Sunday\\n");
else printf("错误\\n");
}', 1, '#include<stdio.h>
void main()
{
int a;
printf("输入一个整数：\\n");
scanf("%d",&a);
if(a==1) printf("Monday\\n");
else if(a==2) printf("Tuesday\\n");
else if(a==3) printf("Wendesday\\n");
else if(a==4) printf("Thursday\\n");
else if(a==5) printf("Friday\\n");
else if(a==6) printf("Saturday\\n");
else if(a==7) printf("Sunday\\n");
else printf("错误\\n");
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-034 编写程序，输入两个整数，和+、-、*、/之中的任意一个运算符，输出计算结果。', 6, 2, 'Basic', '编写程序，输入两个整数，和+、-、*、/之中的任意一个运算符，输出计算结果。', '#include<stdio.h>
void main()
{
float a,b;
char c;
printf("请输入一个运算符：\\n");
scanf("%c",&c);
printf("请输入两个整数：\\n");
scanf("%d,%d",&a,&b);
switch(c)
{
case''+'':printf("%f\\n",a+b);break;
case''-'':printf("%f\\n",a-b);break;
case''*'':printf("%f\\n",a*b);break;
case''/'':printf("%f\\n",a/b);break;
default:printf("错误");
}
}', 1, '#include<stdio.h>
void main()
{
float a,b;
char c;
printf("请输入一个运算符：\\n");
scanf("%c",&c);
printf("请输入两个整数：\\n");
scanf("%d,%d",&a,&b);
switch(c)
{
case''+'':printf("%f\\n",a+b);break;
case''-'':printf("%f\\n",a-b);break;
case''*'':printf("%f\\n",a*b);break;
case''/'':printf("%f\\n",a/b);break;
default:printf("错误");
}
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-035 输入年号，计算这一年的2月份的天数，将结果输出。', 6, 2, 'Basic', '输入年号，计算这一年的2月份的天数，将结果输出。', '#include<stdio.h>
void main()
{
int year,leap;
printf("请输入年份:\\n");
scanf("%d",&year);
if(year%4==0)
{
if(year%100==0)
{
if(year%400==0)
leap=1;
else
leap=0;
}
else
leap=1;
}
else
leap=0;
if(leap)
printf("%d年的2月有29天",year);
else
printf("%d年的2月有28天",year);
}', 1, '#include<stdio.h>
void main()
{
int year,leap;
printf("请输入年份:\\n");
scanf("%d",&year);
if(year%4==0)
{
if(year%100==0)
{
if(year%400==0)
leap=1;
else
leap=0;
}
else
leap=1;
}
else
leap=0;
if(leap)
printf("%d年的2月有29天",year);
else
printf("%d年的2月有28天",year);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-036 输入三角形的三边a,b,c，判断a,b,c，能否构成三角形，若能，计算面积。', 6, 2, 'Basic', '输入三角形的三边a,b,c，判断a,b,c，能否构成三角形，若能，计算面积。', '#include<stdio.h>
#include<math.h>
void main()
{
float a,b,c,area;
double s;
printf("Please enter three sides of a triangle:\\n");
scanf("%f,%f,%f",&a,&b,&c);
s=(a+b+c)/2.0;
area=sqrt(s*(s-a)*(s-b)*(s-c));
if(a+b<c||b+c<a||a+c<b)
printf("They can''t form a triangle.\\n");
else
printf("The area of the triangle is:%f\\n",area);
}
#include<stdio.h>
#define N 10
#define M 10
void main()
{
int i,j,k,m,n,flag1,flag2,a[N][M],max,maxj;
printf("输入行数n[n<10]:");
scanf("%d",&n);
printf("输入列数m[m<10]:");
scanf("%d",&m);
for(i=0;i<n;i++)
{
printf("第%d行\\n",i+1);
for(j=0;j<m;j++)
scanf("%d",&a[j]);
}', 1, '#include<stdio.h>
#include<math.h>
void main()
{
float a,b,c,area;
double s;
printf("Please enter three sides of a triangle:\\n");
scanf("%f,%f,%f",&a,&b,&c);
s=(a+b+c)/2.0;
area=sqrt(s*(s-a)*(s-b)*(s-c));
if(a+b<c||b+c<a||a+c<b)
printf("They can''t form a triangle.\\n");
else
printf("The area of the triangle is:%f\\n",area);
}
#include<stdio.h>
#define N 10
#define M 10
void main()
{
int i,j,k,m,n,flag1,flag2,a[N][M],max,maxj;
printf("输入行数n[n<10]:");
scanf("%d",&n);
printf("输入列数m[m<10]:");
scanf("%d",&m);
for(i=0;i<n;i++)
{
printf("第%d行\\n",i+1);
for(j=0;j<m;j++)
scanf("%d",&a[j]);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-037 输入三角形的三边a,b,c，判断a,b,c，能否构成三角形，何种类型的三角形：等腰、等边、直角、等腰直角、一般。', 6, 2, 'Basic', '输入三角形的三边a,b,c，判断a,b,c，能否构成三角形，何种类型的三角形：等腰、等边、直角、等腰直角、一般。', '#include<stdio.h>
#include<math.h>
void main()
{
float a,b,c;
printf("请输入三角形边长:\\n");
scanf("%f%f%f",&a,&b,&c);
if((a-b>=c)||(b-c>=a)||(c-a>=b)) printf("不能够成三角形\\n");
else if ((a*a+b*b==c*c)||(b*b+c*c==a*a)||(c*c+a*a==b*b))
if ((a==b)||(b==c)||(c==a)) printf("等腰直角三角形\\n");
else printf("直角三角形\\n");
else if ((a==b)&&(b==c)) printf("等边三角形\\n");
else if ((a==b)&&(b!=c)||(c==b)&&(b!=a)||(a==c)&&(c!=a)) printf("等腰三角形\\n");
else printf("一般三角形\\n");
}', 1, '#include<stdio.h>
#include<math.h>
void main()
{
float a,b,c;
printf("请输入三角形边长:\\n");
scanf("%f%f%f",&a,&b,&c);
if((a-b>=c)||(b-c>=a)||(c-a>=b)) printf("不能够成三角形\\n");
else if ((a*a+b*b==c*c)||(b*b+c*c==a*a)||(c*c+a*a==b*b))
if ((a==b)||(b==c)||(c==a)) printf("等腰直角三角形\\n");
else printf("直角三角形\\n");
else if ((a==b)&&(b==c)) printf("等边三角形\\n");
else if ((a==b)&&(b!=c)||(c==b)&&(b!=a)||(a==c)&&(c!=a)) printf("等腰三角形\\n");
else printf("一般三角形\\n");
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-038 输入奖金数a，计算并输出税率、应缴税款和实得奖金数。（用if语句编程）', 6, 2, 'Basic', '输入奖金数a，计算并输出税率、应缴税款和实得奖金数。（用if语句编程）', '#include<stdio.h>
void main()
{
int a;
float r,t,s;
printf("请输入奖金数:\\n");
scanf("%d",&a);
if (a<500) r=0.00;
else if (a<1000) r=0.05;
else if (a<2000) r=0.08;
else if (a<2000) r=0.10;
else r=0.15;
t=a*r;
s=a-t;
printf("税率:%7.2f\\n",r);
printf("税款:%7.2f\\n",t);
printf("实得奖金:%7.2f\\n",s);
}', 1, '#include<stdio.h>
void main()
{
int a;
float r,t,s;
printf("请输入奖金数:\\n");
scanf("%d",&a);
if (a<500) r=0.00;
else if (a<1000) r=0.05;
else if (a<2000) r=0.08;
else if (a<2000) r=0.10;
else r=0.15;
t=a*r;
s=a-t;
printf("税率:%7.2f\\n",r);
printf("税款:%7.2f\\n",t);
printf("实得奖金:%7.2f\\n",s);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-039 输入奖金数a，计算并输出税率、应缴税款和实得奖金数。（用switch语句编程）', 6, 2, 'Basic', '输入奖金数a，计算并输出税率、应缴税款和实得奖金数。（用switch语句编程）', '#include<stdio.h>
void main()
{
int a,e;
float r,t,s;
printf("请输入奖金数:\\n");
scanf("%d",&a);
e=a/500;
switch(e)
{case 0:r=0.00;break;
case 1:r=0.05;break;
case 2:
case 3:r=0.08;break;
case 4:
case 5:
case 6:
case 7:
case 8:
case 9:r=0.10;break;
default:r=0.15;break;
}
t=a*r;
s=a-t;
printf("税率：%7.2f\\n",r);
printf("税款：%7.2f\\n",t);
printf("实得奖金：%7.2f\\n",s);
}', 1, '#include<stdio.h>
void main()
{
int a,e;
float r,t,s;
printf("请输入奖金数:\\n");
scanf("%d",&a);
e=a/500;
switch(e)
{case 0:r=0.00;break;
case 1:r=0.05;break;
case 2:
case 3:r=0.08;break;
case 4:
case 5:
case 6:
case 7:
case 8:
case 9:r=0.10;break;
default:r=0.15;break;
}
t=a*r;
s=a-t;
printf("税率：%7.2f\\n",r);
printf("税款：%7.2f\\n",t);
printf("实得奖金：%7.2f\\n",s);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-040 输入学生的成绩，利用计算机将学生的成绩划分出等级并输出：', 6, 2, 'Basic', '输入学生的成绩，利用计算机将学生的成绩划分出等级并输出：', '90～100：A级； 80～89：B级； 70～79：C级； 60～69：D级； 0～59：E级；
#include<stdio.h>
void main()
{
float m;
printf("输入学生成绩:\\n");
scanf("%f",&m);
if ((m>100)||(m<0)) printf("数据有误\\n");
else if (m>=90) printf("A级\\n");
else if (m>=80) printf("B级\\n");
else if (m>=70) printf("C级\\n");
else if (m>=60) printf("D级\\n");
else printf("E级\\n");
}', 1, '90～100：A级； 80～89：B级； 70～79：C级； 60～69：D级； 0～59：E级；
#include<stdio.h>
void main()
{
float m;
printf("输入学生成绩:\\n");
scanf("%f",&m);
if ((m>100)||(m<0)) printf("数据有误\\n");
else if (m>=90) printf("A级\\n");
else if (m>=80) printf("B级\\n");
else if (m>=70) printf("C级\\n");
else if (m>=60) printf("D级\\n");
else printf("E级\\n");
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-041 编程序，求方程aX2+bX+c=0的解；输入a,b,c.', 6, 2, 'Basic', '编程序，求方程aX2+bX+c=0的解；输入a,b,c.', '#include <stdio.h>
#include <math.h>
void main()
{
float a,b,c,t;
double x1,x2;
printf("请由高次到低次顺序输入系数:\\n");
scanf("%f%f%f",&a,&b,&c);
t=b*b-4*a*c;
if (t<0) printf("方程无实根\\n");
if (t==0)
{
x1=-(b/2/a);
printf("方程有两个相等实根，x1=x2=%5.2f\\n",x1);
};
if (t>0)
{
x1=-(b+sqrt(t))/2/a;
x2=-(b-sqrt(t))/2/a;
printf("方程有两个不等实根，x1=%5.2f,x2=%5.2f\\n",x1,x2);
}
}', 1, '#include <stdio.h>
#include <math.h>
void main()
{
float a,b,c,t;
double x1,x2;
printf("请由高次到低次顺序输入系数:\\n");
scanf("%f%f%f",&a,&b,&c);
t=b*b-4*a*c;
if (t<0) printf("方程无实根\\n");
if (t==0)
{
x1=-(b/2/a);
printf("方程有两个相等实根，x1=x2=%5.2f\\n",x1);
};
if (t>0)
{
x1=-(b+sqrt(t))/2/a;
x2=-(b-sqrt(t))/2/a;
printf("方程有两个不等实根，x1=%5.2f,x2=%5.2f\\n",x1,x2);
}
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-042 编程序，输入4个整数，按由小到大的顺序输出。', 6, 3, 'Basic', '编程序，输入4个整数，按由小到大的顺序输出。', '#include <stdio.h>
#define N 4
void main()
{
int a[N],t,i,j;
printf("输入四个整数:\\n");
for (i=0;i<N;i++)
scanf("%d",&a);
printf("\\n");
for (i=0;i<N-1;i++)
{
for (j=0;j<N-i-1;j++)
if (a[j]>a[j+1])
{
t=a[j];
a[j]=a[j+1];
a[j+1]=t;
}
}
printf("排序后:\\n");
for (i=0;i<N;i++)
printf("%d\\n",a);
}', 1, '#include <stdio.h>
#define N 4
void main()
{
int a[N],t,i,j;
printf("输入四个整数:\\n");
for (i=0;i<N;i++)
scanf("%d",&a);
printf("\\n");
for (i=0;i<N-1;i++)
{
for (j=0;j<N-i-1;j++)
if (a[j]>a[j+1])
{
t=a[j];
a[j]=a[j+1];
a[j+1]=t;
}
}
printf("排序后:\\n");
for (i=0;i<N;i++)
printf("%d\\n",a);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-043 求满足1+2+3+…+n<500中最大的N，并求其和，编写程序实现。', 6, 2, 'Basic', '求满足1+2+3+…+n<500中最大的N，并求其和，编写程序实现。', '#include <stdio.h>
void main()
{
int n=0,sum=0;
while(sum<500)
{
++n;
sum+=n;
}
printf("NֵΪ:%d\\n",n-1);
printf("1+2+3+4+......+%d<500\\n",n-1);
}', 1, '#include <stdio.h>
void main()
{
int n=0,sum=0;
while(sum<500)
{
++n;
sum+=n;
}
printf("NֵΪ:%d\\n",n-1);
printf("1+2+3+4+......+%d<500\\n",n-1);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-044 把100～200之间的不能被3整除的数输出。', 6, 2, 'Basic', '把100～200之间的不能被3整除的数输出。', '#include <stdio.h>
void main()
{
int a;
for (a=100;a<=200;a++)
if (a%3!=0) printf("%d\\t",a);
}', 1, '#include <stdio.h>
void main()
{
int a;
for (a=100;a<=200;a++)
if (a%3!=0) printf("%d\\t",a);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-045 求Fibonacci数列前40个数，每行输出5个，将40个Fibonacci数输出。', 6, 2, 'Basic', '求Fibonacci数列前40个数，每行输出5个，将40个Fibonacci数输出。', '#include <stdio.h>
long f(int);
void main()
{
int n,i=0;
for (n=1;n<41;n++)
{
printf ("%ld\\t",f(n));
i++;
if (i%5==0) printf("\\n");
}
}
long f(int m)
{
if (m==0)
return 0;
if (m==1)
return 1;
else
return f(m-1)+f(m-2);
}', 1, '#include <stdio.h>
long f(int);
void main()
{
int n,i=0;
for (n=1;n<41;n++)
{
printf ("%ld\\t",f(n));
i++;
if (i%5==0) printf("\\n");
}
}
long f(int m)
{
if (m==0)
return 0;
if (m==1)
return 1;
else
return f(m-1)+f(m-2);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-046 按以下规律翻译密码：', 6, 3, 'String', '按以下规律翻译密码：', '将每一个字母变成它后面的字母，例如，将A变成B，B变成C，…，Z变成A，非字母字符不变，“!”作为电文结束标志。
#include <stdio.h>
void main()
{
char ch;
printf("输入字符串:\\n");
while ((ch=getchar())!=''!'')
{
if ((ch>=''a'' && ch<=''z'')||(ch>=''A'' && ch<=''Z''))
if (ch==''z'') ch=''a'';
else if (ch==''Z'') ch=''A'';
else ch=ch+1;
printf("%c",ch);
}
printf("\\n");
}', 1, '将每一个字母变成它后面的字母，例如，将A变成B，B变成C，…，Z变成A，非字母字符不变，“!”作为电文结束标志。
#include <stdio.h>
void main()
{
char ch;
printf("输入字符串:\\n");
while ((ch=getchar())!=''!'')
{
if ((ch>=''a'' && ch<=''z'')||(ch>=''A'' && ch<=''Z''))
if (ch==''z'') ch=''a'';
else if (ch==''Z'') ch=''A'';
else ch=ch+1;
printf("%c",ch);
}
printf("\\n");
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-047 百元买百鸡问题：公鸡每只5元，母鸡每只3元，小鸡3只一元，问一百元买一百只鸡有几种买法。', 6, 2, 'Basic', '百元买百鸡问题：公鸡每只5元，母鸡每只3元，小鸡3只一元，问一百元买一百只鸡有几种买法。', '#include <stdio.h>
void main()
{
int a,b,c,n=0;
for (a=0;a<=20;a++)
for (b=0;b<=33;b++)
for (c=0;c<=100;c++)
if (5*a+3*b+c==100) n++;
printf("一共有%d种\\n",n);
}', 1, '#include <stdio.h>
void main()
{
int a,b,c,n=0;
for (a=0;a<=20;a++)
for (b=0;b<=33;b++)
for (c=0;c<=100;c++)
if (5*a+3*b+c==100) n++;
printf("一共有%d种\\n",n);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-048 使用数组编程，计算出前20项fibonacci数列，要求一行打印5个数。', 6, 2, 'Array', '使用数组编程，计算出前20项fibonacci数列，要求一行打印5个数。', '#include <stdio.h>
long f(int);
void main()
{
int n,i=0;
for (n=1;n<21;n++)
{
printf ("%ld\\t",f(n));
i++;
if (i%5==0) printf("\\n");
}
}
long f(int m)
{
if (m==0)
return 0;
if (m==1)
return 1;
else
return f(m-1)+f(m-2);
}', 1, '#include <stdio.h>
long f(int);
void main()
{
int n,i=0;
for (n=1;n<21;n++)
{
printf ("%ld\\t",f(n));
i++;
if (i%5==0) printf("\\n");
}
}
long f(int m)
{
if (m==0)
return 0;
if (m==1)
return 1;
else
return f(m-1)+f(m-2);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-049 编程序求出两个3*4矩阵的和并将原矩阵和求出的和矩阵按原矩阵的形式分别输出。', 6, 3, 'Array', '编程序求出两个3*4矩阵的和并将原矩阵和求出的和矩阵按原矩阵的形式分别输出。', '#include <stdio.h>
void main()
{
int a[3][4],b[3][4],s[3][4],n,m;
printf("输入数组A:\\n");
for (n=0;n<3;n++)
{
printf("第%d行\\n",n+1);
for (m=0;m<4;m++)
scanf ("%d",&a[n][m]);
}
printf("输入数组B:\\n");
for (n=0;n<3;n++)
{
printf("第%d行\\n",n+1);
for (m=0;m<4;m++)
scanf ("%d",&b[n][m]);
}
for (n=0;n<3;n++)
{
for (m=0;m<4;m++)
s[n][m]=a[n][m]+b[n][m];
}
printf("原数组A:\\n");
for (n=0;n<3;n++)
{
for (m=0;m<4;m++)
printf("%5d",a[n][m]);
printf("\\n");
}
printf("\\n");
printf("原数组B:\\n");
for (n=0;n<3;n++)
{
for (m=0;m<4;m++)
printf("%5d",b[n][m]);
printf("\\n");
}
printf("\\n");
printf("所得数组:\\n");
for (n=0;n<3;n++)
{
for (m=0;m<4;m++)
printf("%5d",s[n][m]);
printf("\\n");
}
}', 1, '#include <stdio.h>
void main()
{
int a[3][4],b[3][4],s[3][4],n,m;
printf("输入数组A:\\n");
for (n=0;n<3;n++)
{
printf("第%d行\\n",n+1);
for (m=0;m<4;m++)
scanf ("%d",&a[n][m]);
}
printf("输入数组B:\\n");
for (n=0;n<3;n++)
{
printf("第%d行\\n",n+1);
for (m=0;m<4;m++)
scanf ("%d",&b[n][m]);
}
for (n=0;n<3;n++)
{
for (m=0;m<4;m++)
s[n][m]=a[n][m]+b[n][m];
}
printf("原数组A:\\n");
for (n=0;n<3;n++)
{
for (m=0;m<4;m++)
printf("%5d",a[n][m]);
printf("\\n");
}
printf("\\n");
printf("原数组B:\\n");
for (n=0;n<3;n++)
{
for (m=0;m<4;m++)
printf("%5d",b[n][m]);
printf("\\n");
}
printf("\\n");
printf("所得数组:\\n");
for (n=0;n<3;n++)
{
for (m=0;m<4;m++)
printf("%5d",s[n][m]);
printf("\\n");
}
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-050 将一个4*3的矩阵转置，并将原矩阵和求出的转置矩阵按原矩阵的形式分别输出。', 6, 3, 'Array', '将一个4*3的矩阵转置，并将原矩阵和求出的转置矩阵按原矩阵的形式分别输出。', '#include <stdio.h>
void main()
{
int a[4][3],b[4][3],s[4][3],n,m;
printf("输入数组A:\\n");
for (n=0;n<4;n++)
{
printf("第%d行\\n",n+1);
for (m=0;m<3;m++)
scanf ("%d",&a[n][m]);
}
printf("输入数组B:\\n");
for (n=0;n<4;n++)
{
printf("第%d行\\n",n+1);
for (m=0;m<3;m++)
scanf ("%d",&b[n][m]);
}
for (n=0;n<4;n++)
{
for (m=0;m<3;m++)
s[n][m]=a[n][m]+b[n][m];
}
printf("原数组A:\\n");
for (n=0;n<4;n++)
{
for (m=0;m<3;m++)
printf("%5d",a[n][m]);
printf("\\n");
}
printf("\\n");
printf("原数组B:\\n");
for (n=0;n<4;n++)
{
for (m=0;m<3;m++)
printf("%5d",b[n][m]);
printf("\\n");
}
printf("\\n");
printf("所得数组:\\n");
for (n=0;n<4;n++)
{
for (m=0;m<3;m++)
printf("%5d",s[n][m]);
printf("\\n");
}
}', 1, '#include <stdio.h>
void main()
{
int a[4][3],b[4][3],s[4][3],n,m;
printf("输入数组A:\\n");
for (n=0;n<4;n++)
{
printf("第%d行\\n",n+1);
for (m=0;m<3;m++)
scanf ("%d",&a[n][m]);
}
printf("输入数组B:\\n");
for (n=0;n<4;n++)
{
printf("第%d行\\n",n+1);
for (m=0;m<3;m++)
scanf ("%d",&b[n][m]);
}
for (n=0;n<4;n++)
{
for (m=0;m<3;m++)
s[n][m]=a[n][m]+b[n][m];
}
printf("原数组A:\\n");
for (n=0;n<4;n++)
{
for (m=0;m<3;m++)
printf("%5d",a[n][m]);
printf("\\n");
}
printf("\\n");
printf("原数组B:\\n");
for (n=0;n<4;n++)
{
for (m=0;m<3;m++)
printf("%5d",b[n][m]);
printf("\\n");
}
printf("\\n");
printf("所得数组:\\n");
for (n=0;n<4;n++)
{
for (m=0;m<3;m++)
printf("%5d",s[n][m]);
printf("\\n");
}
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-051 输入一个字符，如果它是一个大写字母，则把它变成小写字母；如果它是小写字母，则把它变成大写字母；其它字符不变，请编程。', 6, 3, 'String', '输入一个字符，如果它是一个大写字母，则把它变成小写字母；如果它是小写字母，则把它变成大写字母；其它字符不变，请编程。', '#include <stdio.h>
void main()
{
char ch;
ch=getchar();
if ((ch>=65)&&(ch<=90)) ch=ch+32;
else if ((ch>=97)&&(ch<=122)) ch=ch-32;
printf("%c\\n",ch);
}', 1, '#include <stdio.h>
void main()
{
char ch;
ch=getchar();
if ((ch>=65)&&(ch<=90)) ch=ch+32;
else if ((ch>=97)&&(ch<=122)) ch=ch-32;
printf("%c\\n",ch);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-052 已知x和y存在下列对应关系，要求对输入的每个x值，计算出y值，请编程。', 6, 3, 'Basic', '已知x和y存在下列对应关系，要求对输入的每个x值，计算出y值，请编程。', 'y=x*(x+2),2<x<=10; y=2x, -1<x<=2; y=x-1, x<=-1.
#include <stdio.h>
void main()
{
float x,y;
printf("输入X值:");
scanf("%f",&x);
if (x<=-1)
{
y=x-1;
printf ("Y=%6.2f\\n",y);
}
else if (x<=2)
{
y=2*x;
printf ("Y=%6.2f\\n",y);
}
else if (x<=10)
{
y=x*(x+2);
printf ("Y=%6.2f\\n",y);
}
else printf("取值范围出错\\n");
}', 1, 'y=x*(x+2),2<x<=10; y=2x, -1<x<=2; y=x-1, x<=-1.
#include <stdio.h>
void main()
{
float x,y;
printf("输入X值:");
scanf("%f",&x);
if (x<=-1)
{
y=x-1;
printf ("Y=%6.2f\\n",y);
}
else if (x<=2)
{
y=2*x;
printf ("Y=%6.2f\\n",y);
}
else if (x<=10)
{
y=x*(x+2);
printf ("Y=%6.2f\\n",y);
}
else printf("取值范围出错\\n");
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-053 已知x和y存在下列对应关系，要求对输入的每个x值，计算出y值，请编程。', 6, 3, 'Basic', '已知x和y存在下列对应关系，要求对输入的每个x值，计算出y值，请编程。', 'y=0,x=a||x=-a; y=sqrt(a*a-x*x), -a<x<a; y=x,x<-a||x>a.
#include <stdio.h>
#include <math.h>
void main()
{
float x,y,a;
printf("输入A值[A>0]:");
scanf("%f",&a);
printf("输入X值:");
scanf("%f",&x);
if ((x==a)||(x==-a)) y=0;
else if ((x<a)&&(x>-a)) y=sqrt(a*a-x*x);
else if ((x<-a)||(x>a)) y=x;
printf ("Y=%6.2f\\n",y);
}', 1, 'y=0,x=a||x=-a; y=sqrt(a*a-x*x), -a<x<a; y=x,x<-a||x>a.
#include <stdio.h>
#include <math.h>
void main()
{
float x,y,a;
printf("输入A值[A>0]:");
scanf("%f",&a);
printf("输入X值:");
scanf("%f",&x);
if ((x==a)||(x==-a)) y=0;
else if ((x<a)&&(x>-a)) y=sqrt(a*a-x*x);
else if ((x<-a)||(x>a)) y=x;
printf ("Y=%6.2f\\n",y);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-054 编程判断对输入的任何一个年份是否是闰年，将结果输出。', 6, 3, 'Basic', '编程判断对输入的任何一个年份是否是闰年，将结果输出。', '#include <stdio.h>
void main()
{
int n;
printf("输入年份:");
scanf("%d",&n);
if (n%4==0&&n%100!=0||n%400==0) printf("闰年\\n");
else printf ("不是闰年\\n");
}', 1, '#include <stdio.h>
void main()
{
int n;
printf("输入年份:");
scanf("%d",&n);
if (n%4==0&&n%100!=0||n%400==0) printf("闰年\\n");
else printf ("不是闰年\\n");
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-055 请编制程序要求输入整数a和b，若a*a+b*b大于100，则输出a*a+b*b百位以上的数字，否则输出两数之和。', 6, 3, 'Basic', '请编制程序要求输入整数a和b，若a*a+b*b大于100，则输出a*a+b*b百位以上的数字，否则输出两数之和。', '#include <stdio.h>
void main ()
{
int a,b,c,d;
printf("请输入两个整数:\\n");
scanf("%d,%d",&a,&b);
printf("你输入的两个数为：%d,%d\\n",a,b);
c=a*a+b*b;
if(c>=100)
{
d=c/100;
printf("a*a+b*b的百位以上的数为：%d\\n",d);
}
else
printf("a*a+b*b=%d/n",c);
}', 1, '#include <stdio.h>
void main ()
{
int a,b,c,d;
printf("请输入两个整数:\\n");
scanf("%d,%d",&a,&b);
printf("你输入的两个数为：%d,%d\\n",a,b);
c=a*a+b*b;
if(c>=100)
{
d=c/100;
printf("a*a+b*b的百位以上的数为：%d\\n",d);
}
else
printf("a*a+b*b=%d/n",c);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-056 请编制程序判断输入的正整数是否既是5又是7的整倍数。若是，则输出yes；否则输出no.', 6, 3, 'Basic', '请编制程序判断输入的正整数是否既是5又是7的整倍数。若是，则输出yes；否则输出no.', '#include <stdio.h>
void main()
{
int a;
printf("请输入要验证的数:\\n");
scanf("%d",&a);
if(a%5==0&&a%7==0)
printf("Yes\\n");
else
printf("No\\n");
}', 1, '#include <stdio.h>
void main()
{
int a;
printf("请输入要验证的数:\\n");
scanf("%d",&a);
if(a%5==0&&a%7==0)
printf("Yes\\n");
else
printf("No\\n");
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-057 编程实现：计算1到100之间的奇数之和及偶数之和。', 6, 3, 'Basic', '编程实现：计算1到100之间的奇数之和及偶数之和。', '#include <stdio.h>
void main ()
{
int n,even=0,odd=0;
for(n=1;n<=50;n++)
{
even+=2*n;
odd+=2*n-1;
}
printf("1到100间的偶数的和为%d:\\n",even);
printf("1到100间的奇数的和为%d:\\n",odd);
}', 1, '#include <stdio.h>
void main ()
{
int n,even=0,odd=0;
for(n=1;n<=50;n++)
{
even+=2*n;
odd+=2*n-1;
}
printf("1到100间的偶数的和为%d:\\n",even);
printf("1到100间的奇数的和为%d:\\n",odd);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-058 请编程实现：求100个任意整数的累加和。', 6, 3, 'Basic', '请编程实现：求100个任意整数的累加和。', '', 1, 'No analysis text in source document.', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-059 请编程实现：求1到100之间所有能被3整除，但不能被5整除的数的和。', 6, 3, 'Basic', '请编程实现：求1到100之间所有能被3整除，但不能被5整除的数的和。', '', 1, 'No analysis text in source document.', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-060 请编程实现：输入任意正整数n，计算n!并将结果输出，输出结果中没有小数部分', 6, 3, 'Basic', '请编程实现：输入任意正整数n，计算n!并将结果输出，输出结果中没有小数部分', 'xiang6963
2008-11-12 23:43:16', 1, 'xiang6963
2008-11-12 23:43:16', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-061 请编程实现：计算1至50中是7的倍数的数值之和。', 6, 3, 'Basic', '请编程实现：计算1至50中是7的倍数的数值之和。', '', 1, 'No analysis text in source document.', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-062 请编程实现：对任意100个整数，统计0的个数及正数的累加和。', 6, 3, 'Basic', '请编程实现：对任意100个整数，统计0的个数及正数的累加和。', '#include <stdio.h>
#define N 100
void main ()
{
int a,sum=0,i,frequency=0;
int s[N];
printf("请输入数据:\\n");
for(i=0;i<N;i++)
scanf("%d",&s);
printf("\\n原始数据为：\\n");
for(i=0;i<N;i++)
printf("%d\\n",s);
for(i=0;i<N;i++)
{
if(s==0)
frequency+=1;
if(s>0)
{
a=s;
sum+=a;
}
}
printf("这些数中0的个数为%d\\n",frequency);
printf("这些数中所有正数的和为%d\\n",sum);
}', 1, '#include <stdio.h>
#define N 100
void main ()
{
int a,sum=0,i,frequency=0;
int s[N];
printf("请输入数据:\\n");
for(i=0;i<N;i++)
scanf("%d",&s);
printf("\\n原始数据为：\\n");
for(i=0;i<N;i++)
printf("%d\\n",s);
for(i=0;i<N;i++)
{
if(s==0)
frequency+=1;
if(s>0)
{
a=s;
sum+=a;
}
}
printf("这些数中0的个数为%d\\n",frequency);
printf("这些数中所有正数的和为%d\\n",sum);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-063 请编程实现：输入两个整数，判断它们之间的关系（=,<,>等），并清楚地将比较结果输出。', 6, 3, 'Basic', '请编程实现：输入两个整数，判断它们之间的关系（=,<,>等），并清楚地将比较结果输出。', '', 1, 'No analysis text in source document.', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-064 请编程实现：输入几个整数，判断其中偶数的个数，并输出结果（要求：数据的个数及原始数据由键盘输入）。', 6, 4, 'Array', '请编程实现：输入几个整数，判断其中偶数的个数，并输出结果（要求：数据的个数及原始数据由键盘输入）。', '#include <stdio.h>
#define N 20
void enter (int a[], int n)
{
int i;
printf("请输入数据:\\n");
for (i=0;i<n;i++)
scanf("%d",&a);
printf("\\n原始数据为:\\n");
for (i=0;i<n;i++)
printf("%d",a);
printf("\\n");
}
void main()
{
void enter (int [], int );
int i;
int a[N],n;
printf("\\n输入你所要的数组元素个数:\\n");
scanf("%d",&n);
enter (a,n);
for(i=0;i<n;i++)
{
if (a%2==0)
printf ("其中的偶数是%d\\n",a);
}
}', 1, '#include <stdio.h>
#define N 20
void enter (int a[], int n)
{
int i;
printf("请输入数据:\\n");
for (i=0;i<n;i++)
scanf("%d",&a);
printf("\\n原始数据为:\\n");
for (i=0;i<n;i++)
printf("%d",a);
printf("\\n");
}
void main()
{
void enter (int [], int );
int i;
int a[N],n;
printf("\\n输入你所要的数组元素个数:\\n");
scanf("%d",&n);
enter (a,n);
for(i=0;i<n;i++)
{
if (a%2==0)
printf ("其中的偶数是%d\\n",a);
}
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-065 请编程实现：输入几个整数，判断其中奇数的个数，并输出奇数的累加和。（要求：数据的个数及原始数据由键盘输入）', 6, 4, 'Array', '请编程实现：输入几个整数，判断其中奇数的个数，并输出奇数的累加和。（要求：数据的个数及原始数据由键盘输入）', '#include <stdio.h>
#define N 20
void enter (int a[], int n)
{
int i;
printf("请输入数据:\\n");
for (i=0;i<n;i++)
scanf("%d",&a);
printf("\\n原始数据为:\\n");
for (i=0;i<n;i++)
printf("%2d",a);
printf("\\n");
}
void main()
{
void enter (int [], int );
int i;
int a[N],n,sum=0;
printf("\\n输入你所要的数组元素个数:\\n");
scanf("%d",&n);
enter (a,n);
for(i=0;i<n;i++)
{
if (a%2==1)
{printf ("其中的奇数是%d\\n",a);
sum=sum+a;}
}printf ("奇数和是%d\\n",sum);
}', 1, '#include <stdio.h>
#define N 20
void enter (int a[], int n)
{
int i;
printf("请输入数据:\\n");
for (i=0;i<n;i++)
scanf("%d",&a);
printf("\\n原始数据为:\\n");
for (i=0;i<n;i++)
printf("%2d",a);
printf("\\n");
}
void main()
{
void enter (int [], int );
int i;
int a[N],n,sum=0;
printf("\\n输入你所要的数组元素个数:\\n");
scanf("%d",&n);
enter (a,n);
for(i=0;i<n;i++)
{
if (a%2==1)
{printf ("其中的奇数是%d\\n",a);
sum=sum+a;}
}printf ("奇数和是%d\\n",sum);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-066 有一个两位数XY，X是十位，Y是个位；给出X+Y的值和X*Y的值；编程实现让用户猜测这个两位数十多少？根据猜测给出不同的提示。', 6, 3, 'Basic', '有一个两位数XY，X是十位，Y是个位；给出X+Y的值和X*Y的值；编程实现让用户猜测这个两位数十多少？根据猜测给出不同的提示。', '#include <stdio.h>
void main()
{
printf ("x+y=2\\n");
printf("x*y=1\\n");
printf("请输入你所猜得数字\\n");
int a;
scanf("%d", &a);
for (;a!=11;)
{printf("you are not right\\n");
scanf("%d", &a);}
printf ("you are right\\n");
}', 1, '#include <stdio.h>
void main()
{
printf ("x+y=2\\n");
printf("x*y=1\\n");
printf("请输入你所猜得数字\\n");
int a;
scanf("%d", &a);
for (;a!=11;)
{printf("you are not right\\n");
scanf("%d", &a);}
printf ("you are right\\n");
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-067 键盘输入的一个两位数XY，X是十位，Y是个位；请编程计算X+Y的值和X*Y的值。', 6, 3, 'Basic', '键盘输入的一个两位数XY，X是十位，Y是个位；请编程计算X+Y的值和X*Y的值。', '#include <stdio.h>
void main ()
{
printf ("请输入一个两位数\\n");
int a,x,y;
scanf ("%d",&a);
x=a/10;
y=a%10;
printf ("x+y=%d\\n",x+y);
printf("x*y=%d\\n"x*y);
}', 1, '#include <stdio.h>
void main ()
{
printf ("请输入一个两位数\\n");
int a,x,y;
scanf ("%d",&a);
x=a/10;
y=a%10;
printf ("x+y=%d\\n",x+y);
printf("x*y=%d\\n"x*y);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-068 用for循环语句计算1到20的和，并将结果输出。', 6, 3, 'Basic', '用for循环语句计算1到20的和，并将结果输出。', '#include <stdio.h>
void main()
{
int i=1,sum=0;
for (;i<=20;)
{
sum=sum+i;
i++;
}
printf("1到20的和是%d\\n",sum);
}', 1, '#include <stdio.h>
void main()
{
int i=1,sum=0;
for (;i<=20;)
{
sum=sum+i;
i++;
}
printf("1到20的和是%d\\n",sum);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-069 从键盘输入一行字符，统计出输入的字符个数（注：不要使用strlun函数编程）。', 6, 4, 'String', '从键盘输入一行字符，统计出输入的字符个数（注：不要使用strlun函数编程）。', '#include <stdio.h>
void main()
{ int alphabet=0,i=0;
char str[100];
printf ("请输入一个字符串\\n");
gets(str);
for (i=0;str!=''\\0'';i++)
++alphabet;
printf ("有%d个字母\\n",alphabet);
}', 1, '#include <stdio.h>
void main()
{ int alphabet=0,i=0;
char str[100];
printf ("请输入一个字符串\\n");
gets(str);
for (i=0;str!=''\\0'';i++)
++alphabet;
printf ("有%d个字母\\n",alphabet);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-070 编程实现：任意输入10个数，计算所有正数的和，负数的和以及10个数的和。', 6, 3, 'Basic', '编程实现：任意输入10个数，计算所有正数的和，负数的和以及10个数的和。', '#include <stdio.h>
void main()
{ int i;
float a[10];
printf("请输入任意十个数\\n");
for (i=0;i<10;i++)
{
printf("a[%d]=",i);
scanf("%f",&a);
}
printf("\\n");
printf("\\n原始数据为:\\n");
for (i=0;i<10;i++)
printf("%3f",a);
float sum1=0, sum2=0, sum3=0;
for (i=0;i<10;i++)
{if (a>0) sum1=sum1+a;
else sum2=sum2+a;
sum3=sum1+sum2;}
printf("正数的和是%f",sum1);
printf("负数的和是%f",sum2);
printf("总和是%f",sum3);
}', 1, '#include <stdio.h>
void main()
{ int i;
float a[10];
printf("请输入任意十个数\\n");
for (i=0;i<10;i++)
{
printf("a[%d]=",i);
scanf("%f",&a);
}
printf("\\n");
printf("\\n原始数据为:\\n");
for (i=0;i<10;i++)
printf("%3f",a);
float sum1=0, sum2=0, sum3=0;
for (i=0;i<10;i++)
{if (a>0) sum1=sum1+a;
else sum2=sum2+a;
sum3=sum1+sum2;}
printf("正数的和是%f",sum1);
printf("负数的和是%f",sum2);
printf("总和是%f",sum3);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-071 编程实现：求任意20个数中的正数之和及正数的个数，并将结果输出。', 6, 3, 'Basic', '编程实现：求任意20个数中的正数之和及正数的个数，并将结果输出。', '#include <stdio.h>
void main()
{ int i;
float a[20];
printf("请输入任意二十个数\\n");
for (i=0;i<20;i++)
{
printf("a[%d]=",i);
scanf("%f",&a);
}
int m=0;
float sum=0;
for (i=0;i<20;i++)
{
if (a>0)
m++;
}
for (i=0;i<20;i++)
sum=sum+a;
printf("正数的个数是%d",m);
printf("所有数的和是%f",sum);
}', 1, '#include <stdio.h>
void main()
{ int i;
float a[20];
printf("请输入任意二十个数\\n");
for (i=0;i<20;i++)
{
printf("a[%d]=",i);
scanf("%f",&a);
}
int m=0;
float sum=0;
for (i=0;i<20;i++)
{
if (a>0)
m++;
}
for (i=0;i<20;i++)
sum=sum+a;
printf("正数的个数是%d",m);
printf("所有数的和是%f",sum);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-072 编程实现：对键盘输入的任意一个四位正整数，计算各位数字平方和。', 6, 3, 'Basic', '编程实现：对键盘输入的任意一个四位正整数，计算各位数字平方和。', '如：2345 则：计算2*2+3*3+4*4+5*5
#include <stdio.h>
#include <math.h>
void main ()
{
printf ("请输入一个四位数\\n");
int x,a,b,c,d,s;
scanf ("%d",&x);
a=x/1000;
b=x/100-10*a;
c=x/10-100*a-10*b;
d=x%10;
printf ("a=%d\\n",a);
printf("b=%d\\n",b);
printf ("c=%d\\n",c);
printf ("d=%d\\n",d);
s=a*a+b*b+c*c+d*d;
printf("各位数字的平方和是%d\\n",s);
}', 1, '如：2345 则：计算2*2+3*3+4*4+5*5
#include <stdio.h>
#include <math.h>
void main ()
{
printf ("请输入一个四位数\\n");
int x,a,b,c,d,s;
scanf ("%d",&x);
a=x/1000;
b=x/100-10*a;
c=x/10-100*a-10*b;
d=x%10;
printf ("a=%d\\n",a);
printf("b=%d\\n",b);
printf ("c=%d\\n",c);
printf ("d=%d\\n",d);
s=a*a+b*b+c*c+d*d;
printf("各位数字的平方和是%d\\n",s);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-073 有1020个西瓜，第一天卖一半多两个，以后每天卖剩下的一半多两个，问几天以后能卖完，请编程。', 6, 3, 'Basic', '有1020个西瓜，第一天卖一半多两个，以后每天卖剩下的一半多两个，问几天以后能卖完，请编程。', '#include <stdio.h>
void main()
{
int a=1020,c=0;
do
{a=a/2-2;
c++;}
while (a!=0);
printf("c=%d",c);
}', 1, '#include <stdio.h>
void main()
{
int a=1020,c=0;
do
{a=a/2-2;
c++;}
while (a!=0);
printf("c=%d",c);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-074 编程实现：打印100以内个位数为6且能被3整除的所有数。', 6, 2, 'Basic', '编程实现：打印100以内个位数为6且能被3整除的所有数。', '#include <stdio.h>
void main()
{int i;
for (i=0;i<100;i++)
{if (i%10==6&&i%3==0)
printf("%d\\n",i);
}
}', 1, '#include <stdio.h>
void main()
{int i;
for (i=0;i<100;i++)
{if (i%10==6&&i%3==0)
printf("%d\\n",i);
}
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-075 编程实现：从键盘输入若干个整数（数据个数应不少于50），其值在0至4的范围内，用-1作为输入结束的标志，统计每个整数的个数。', 6, 3, 'Basic', '编程实现：从键盘输入若干个整数（数据个数应不少于50），其值在0至4的范围内，用-1作为输入结束的标志，统计每个整数的个数。', '', 1, 'No analysis text in source document.', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-076 编写一个函数sort，将放到一维数组中的若干个数按从大到小的顺序排序；在主函数中输入若干个数到一个一维数组中，调用sort，对该数组进行排序，在主函数中将原数组和排好序的数组输出。', 6, 4, 'Array', '编写一个函数sort，将放到一维数组中的若干个数按从大到小的顺序排序；在主函数中输入若干个数到一个一维数组中，调用sort，对该数组进行排序，在主函数中将原数组和排好序的数组输出。', '', 1, 'No analysis text in source document.', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-077 输入一个正整数，将其用质因子的乘积表示，并输出结果，格式为：12=2×2×3。', 6, 3, 'Basic', '输入一个正整数，将其用质因子的乘积表示，并输出结果，格式为：12=2×2×3。', '', 1, 'No analysis text in source document.', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-078 输入一个正整数，求出它的质因子的和，并输出结果，格式为：12的质因子和=2+2+3=7。', 6, 3, 'Basic', '输入一个正整数，求出它的质因子的和，并输出结果，格式为：12的质因子和=2+2+3=7。', '', 1, 'No analysis text in source document.', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-079 编写一个函数，判断一个正整数是否为完数：如果一个数的除它本身以外的所有因数之和等于它本身，则它就是完数。主函数中找出1000以内的所有完数。', 6, 4, 'Function', '编写一个函数，判断一个正整数是否为完数：如果一个数的除它本身以外的所有因数之和等于它本身，则它就是完数。主函数中找出1000以内的所有完数。', '', 1, 'No analysis text in source document.', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-080 编写函数GCD，求两个正整数的最大公约数，主函数中输入任意5个正整数，调用函数GCD，求出这5个数的最大公约数和最小公倍数。', 6, 3, 'Function', '编写函数GCD，求两个正整数的最大公约数，主函数中输入任意5个正整数，调用函数GCD，求出这5个数的最大公约数和最小公倍数。', '', 1, 'No analysis text in source document.', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-081 编函数isprime判断正整m是否为素数；如果是素数，返回正整数1，否则返回0；主函数中调用isprime，找出2到1000之间的所有素数。', 6, 5, 'Function', '编函数isprime判断正整m是否为素数；如果是素数，返回正整数1，否则返回0；主函数中调用isprime，找出2到1000之间的所有素数。', '#include<stdio.h>
#include<math.h>
int isprime(int);
void main()
{
int m;
for(m=2;m<=1000;m++)
if(isprime(m))
printf("%5d",m);
}
int isprime(int a)
{
int i;
for(i=2;i<=sqrt(a);i++)
if(a%i==0) return 0;
return 1;
}', 1, '#include<stdio.h>
#include<math.h>
int isprime(int);
void main()
{
int m;
for(m=2;m<=1000;m++)
if(isprime(m))
printf("%5d",m);
}
int isprime(int a)
{
int i;
for(i=2;i<=sqrt(a);i++)
if(a%i==0) return 0;
return 1;
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-082 一维数组a中的若干个数已按从小到大的顺序有序；在主函数中输入一个数，将其插入到该数组中，使得原数组依然按原序有序，分别输入原数组和插入新元素之后的数组。', 6, 5, 'Array', '一维数组a中的若干个数已按从小到大的顺序有序；在主函数中输入一个数，将其插入到该数组中，使得原数组依然按原序有序，分别输入原数组和插入新元素之后的数组。', '#include<stdio.h>
void main()
{
int a[21],i,j,n,p,temp;
printf("请输入数组元素的个数:");
scanf("%d",&n);
printf("请输入%d个整数:\\n",n);
for(i=0;i<n;i++)
scanf("%d",&a);
for(i=1;i<=n-1;i++)
{
for(j=0;j<=n-1-i;j++)
if(a[j]>a[j+1])
{
temp=a[j];
a[j]=a[j+1];
a[j+1]=temp;
}
}
printf("原数组为:\\n");
for(i=0;i<=n-1;i++)
printf("%5d",a);
printf("\\n请输入插入的数:");
scanf("%d",&p);
for(i=0;i<=n-1;i++)
if(p<a)
{
temp=a;
a=p;
p=temp;
}
a[n]=p;
printf("插入元素后的数组为:\\n");
for(i=0;i<=n;i++)
printf("%5d",a);
}', 1, '#include<stdio.h>
void main()
{
int a[21],i,j,n,p,temp;
printf("请输入数组元素的个数:");
scanf("%d",&n);
printf("请输入%d个整数:\\n",n);
for(i=0;i<n;i++)
scanf("%d",&a);
for(i=1;i<=n-1;i++)
{
for(j=0;j<=n-1-i;j++)
if(a[j]>a[j+1])
{
temp=a[j];
a[j]=a[j+1];
a[j+1]=temp;
}
}
printf("原数组为:\\n");
for(i=0;i<=n-1;i++)
printf("%5d",a);
printf("\\n请输入插入的数:");
scanf("%d",&p);
for(i=0;i<=n-1;i++)
if(p<a)
{
temp=a;
a=p;
p=temp;
}
a[n]=p;
printf("插入元素后的数组为:\\n");
for(i=0;i<=n;i++)
printf("%5d",a);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-083 有5个国家名，编程实现按字母先后顺序排序，然后分别输出原数组和排序之后的数组。', 6, 5, 'Array', '有5个国家名，编程实现按字母先后顺序排序，然后分别输出原数组和排序之后的数组。', '#include<stdio.h>
#include<string.h>
void main()
{
char name[5][15],temp[15];
int i,j;
printf("请输入国家名:\\n");
for(i=0;i<5;i++)
gets(name);
for(i=0;i<5;i++)
for(j=0;j<4-i;j++)
if(strcmp(name,name[j+1])>0)
{
strcpy(temp,name[j]);
strcpy(name[j],name[j+1]);
strcpy(name[j+1],temp);
}
printf("国家名排序后为:\\n");
for(i=0;i<5;i++)
printf("%s\\n",name);
}', 1, '#include<stdio.h>
#include<string.h>
void main()
{
char name[5][15],temp[15];
int i,j;
printf("请输入国家名:\\n");
for(i=0;i<5;i++)
gets(name);
for(i=0;i<5;i++)
for(j=0;j<4-i;j++)
if(strcmp(name,name[j+1])>0)
{
strcpy(temp,name[j]);
strcpy(name[j],name[j+1]);
strcpy(name[j+1],temp);
}
printf("国家名排序后为:\\n");
for(i=0;i<5;i++)
printf("%s\\n",name);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-084 有一行文字，要求删去其中的某个字符，此行文字和要删的字符均由键盘输入，分别输出原文字和删除之后的文字（注：原文字中的所有和要删除字符相同的字符完全删除）。', 6, 4, 'String', '有一行文字，要求删去其中的某个字符，此行文字和要删的字符均由键盘输入，分别输出原文字和删除之后的文字（注：原文字中的所有和要删除字符相同的字符完全删除）。', '#include<stdio.h>
void main()
{
int i,j;
char a[10],N=''n'';
printf("请输入一行9个的文字:\\n");
gets(a);
printf("原文字为:\\n");
puts(a);
for(i=0;i<=9;i++)
if(a==N)
for(j=i;j<=9;j++)
a[j]=a[j+1];
printf("删除后文字为:\\n");
puts(a);
}', 1, '#include<stdio.h>
void main()
{
int i,j;
char a[10],N=''n'';
printf("请输入一行9个的文字:\\n");
gets(a);
printf("原文字为:\\n");
puts(a);
for(i=0;i<=9;i++)
if(a==N)
for(j=i;j<=9;j++)
a[j]=a[j+1];
printf("删除后文字为:\\n");
puts(a);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-085 打印如图所示的杨辉三角，要求打印出n行，n由键盘输入。', 6, 3, 'Basic', '打印如图所示的杨辉三角，要求打印出n行，n由键盘输入。', '1
1 1
1 2 1
1 3 3 1
1 4 6 4 1
1 5 10 10 5 1
………
#include<stdio.h>
void main()
{
int i,j,N,a[21][21];
printf("请输入打印的行数:\\n");
scanf("%d",&N);
for(i=1;i<N+1;i++)
{
a[1]=1;
a=1;
}
for(i=3;i<N+1;i++)
for(j=2;j<=i-1;j++)
a[j]=a[i-1][j-1]+a[i-1][j];
for(i=1;i<N+1;i++)
{
for(j=1;j<=i;j++)
printf("%6d",a[j]);
printf("\\n");
}
printf("\\n");
}', 1, '1
1 1
1 2 1
1 3 3 1
1 4 6 4 1
1 5 10 10 5 1
………
#include<stdio.h>
void main()
{
int i,j,N,a[21][21];
printf("请输入打印的行数:\\n");
scanf("%d",&N);
for(i=1;i<N+1;i++)
{
a[1]=1;
a=1;
}
for(i=3;i<N+1;i++)
for(j=2;j<=i-1;j++)
a[j]=a[i-1][j-1]+a[i-1][j];
for(i=1;i<N+1;i++)
{
for(j=1;j<=i;j++)
printf("%6d",a[j]);
printf("\\n");
}
printf("\\n");
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-086 编一个函数实现将一个整型的一维数组中的数逆序存放，不使用辅助数组。主函数输入一个整型的一维数组，调用上述函数，将该数组逆置，将结果输出。', 6, 5, 'Array', '编一个函数实现将一个整型的一维数组中的数逆序存放，不使用辅助数组。主函数输入一个整型的一维数组，调用上述函数，将该数组逆置，将结果输出。', '#include<stdio.h>
#define N 10
void main()
{
int a[N],i,temp;
printf("enter array a:\\n");
for(i=0;i<N;i++)
scanf("%d",&a);
printf("array a:\\n");
for(i=0;i<N;i++)
printf("%4d",a);
for(i=0;i<N/2;i++)
{
temp=a;
a=a[N-i-1];
a[N-i-1]=temp;
}
printf("\\nNow,array a:\\n");
for(i=0;i<N;i++)
printf("%4d",a);
printf("\\n");
}', 1, '#include<stdio.h>
#define N 10
void main()
{
int a[N],i,temp;
printf("enter array a:\\n");
for(i=0;i<N;i++)
scanf("%d",&a);
printf("array a:\\n");
for(i=0;i<N;i++)
printf("%4d",a);
for(i=0;i<N/2;i++)
{
temp=a;
a=a[N-i-1];
a[N-i-1]=temp;
}
printf("\\nNow,array a:\\n");
for(i=0;i<N;i++)
printf("%4d",a);
printf("\\n");
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-087 编写一个函数convert，求一个方阵的转置矩阵；主函数中输入方阵的阶数和方阵，在主函数中将原矩阵和转置矩阵按原格式输出。', 6, 5, 'Array', '编写一个函数convert，求一个方阵的转置矩阵；主函数中输入方阵的阶数和方阵，在主函数中将原矩阵和转置矩阵按原格式输出。', '#include<stdio.h>
void main()
{
void convert(int [10][10]);
int a[10][10],p,q,i,j;
printf("请输入矩阵的行和列:\\n");
scanf("%d",&p);
scanf("%d",&q);
printf("请输入矩阵的元素值:\\n");
for(i=0;i<p;i++)
for(j=0;j<q;j++)
scanf("%d",&a[j]);
printf("原矩阵为:\\n");
for(i=0;i<p;i++)
{
for(j=0;j<q;j++)
printf("%5d",a[j]);
printf("\\n");
}
convert(a);
printf("转置矩阵为:\\n");
for(i=0;i<q;i++)
{
for(j=0;j<p;j++)
printf("%5d",a[j]);
printf("\\n");
}
}
void convert(int a[10][10])
{
int i,j,t;
for(i=0;i<10;i++)
for(j=i+1;j<10;j++)
{
t=a[j];
a[j]=a[j];
a[j]=t;
}
}', 1, '#include<stdio.h>
void main()
{
void convert(int [10][10]);
int a[10][10],p,q,i,j;
printf("请输入矩阵的行和列:\\n");
scanf("%d",&p);
scanf("%d",&q);
printf("请输入矩阵的元素值:\\n");
for(i=0;i<p;i++)
for(j=0;j<q;j++)
scanf("%d",&a[j]);
printf("原矩阵为:\\n");
for(i=0;i<p;i++)
{
for(j=0;j<q;j++)
printf("%5d",a[j]);
printf("\\n");
}
convert(a);
printf("转置矩阵为:\\n");
for(i=0;i<q;i++)
{
for(j=0;j<p;j++)
printf("%5d",a[j]);
printf("\\n");
}
}
void convert(int a[10][10])
{
int i,j,t;
for(i=0;i<10;i++)
for(j=i+1;j<10;j++)
{
t=a[j];
a[j]=a[j];
a[j]=t;
}
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-088 求∏值，精度为10-5：∏/4≈1-1/3+1/5-1/7+……', 6, 4, 'Basic', '求∏值，精度为10-5：∏/4≈1-1/3+1/5-1/7+……', '#include<stdio.h>
void main()
{
float a=1.0,b;
int i;
for(i=1;1.0/(2*i+1)>0.00001;i++)
{
if(i%2!=0)
a-=1.0/(2*i+1);
else
a+=1.0/(2*i+1);
}
b=4.0*a;
printf("b的值为:%f\\n",b);
}', 1, '#include<stdio.h>
void main()
{
float a=1.0,b;
int i;
for(i=1;1.0/(2*i+1)>0.00001;i++)
{
if(i%2!=0)
a-=1.0/(2*i+1);
else
a+=1.0/(2*i+1);
}
b=4.0*a;
printf("b的值为:%f\\n",b);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-089 用公式计算：e≈1+1/1!+1/2! …+1/n!，精度为10-6。', 6, 4, 'Basic', '用公式计算：e≈1+1/1!+1/2! …+1/n!，精度为10-6。', '#include<stdio.h>
float fun(int);
void main()
{
int i;
float e=1.0;
for(i=1;fun(i)>0.00000001;i++)
e+=fun(i);
printf("e=%f\\n",e);
}
float fun(int n)
{
int i;
float term=1.0;
for(i=1;i<=n;i++)
term/=i;
return term;
}', 1, '#include<stdio.h>
float fun(int);
void main()
{
int i;
float e=1.0;
for(i=1;fun(i)>0.00000001;i++)
e+=fun(i);
printf("e=%f\\n",e);
}
float fun(int n)
{
int i;
float term=1.0;
for(i=1;i<=n;i++)
term/=i;
return term;
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-090 有一分数序列', 6, 4, 'Basic', '有一分数序列', '2/1，3/2，5/3，8/5，13/8，21/13
求该序列的前20项之和。
#include<stdio.h>
void main()
{
int i;
float a[22], b[20],p=0.0;
a[0]=1.0;
a[1]=1.0;
for(i=2;i<22;i++)
a=a[i-1]+a[i-2];
for(i=0;i<20;i++)
b=a[i+2]/a[i+1];
for(i=0;i<20;i++)
p+=b;
printf("%f\\n",p);
}
xiang6963
2008-11-12 23:43:51', 1, '2/1，3/2，5/3，8/5，13/8，21/13
求该序列的前20项之和。
#include<stdio.h>
void main()
{
int i;
float a[22], b[20],p=0.0;
a[0]=1.0;
a[1]=1.0;
for(i=2;i<22;i++)
a=a[i-1]+a[i-2];
for(i=0;i<20;i++)
b=a[i+2]/a[i+1];
for(i=0;i<20;i++)
p+=b;
printf("%f\\n",p);
}
xiang6963
2008-11-12 23:43:51', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-091 编一个子函数GCD，求两个正整数的最大公约数，主程序输入n个自然数，调GCD，求出这n个数的最大公约数。', 6, 4, 'Array', '编一个子函数GCD，求两个正整数的最大公约数，主程序输入n个自然数，调GCD，求出这n个数的最大公约数。', '#include
int GCD(int,int);
void main()
{
int a[100],i,n,k;
printf("请输入数组元素的个数:\\n");
scanf("%d",&n);
printf("请输入%d个正整数:\\n",n);
for(i=0;i
scanf("%d",&a);
k=GCD(a[0],a[1]);
for(i=2;i
k=GCD(k,a);
printf("输入的%d个正整数的最大公约数是:%d\\n",n,k);
}
int GCD(int x,int y)
{
int i,min,p,q,gcd;
if(x<=y)
min=x;
else
min=y;
for(i=1;i<=min;i++)
{
p=x%i;
q=y%i;
if(p==0&&q==0)
gcd=i;
}
return gcd;
}', 1, '#include
int GCD(int,int);
void main()
{
int a[100],i,n,k;
printf("请输入数组元素的个数:\\n");
scanf("%d",&n);
printf("请输入%d个正整数:\\n",n);
for(i=0;i
scanf("%d",&a);
k=GCD(a[0],a[1]);
for(i=2;i
k=GCD(k,a);
printf("输入的%d个正整数的最大公约数是:%d\\n",n,k);
}
int GCD(int x,int y)
{
int i,min,p,q,gcd;
if(x<=y)
min=x;
else
min=y;
for(i=1;i<=min;i++)
{
p=x%i;
q=y%i;
if(p==0&&q==0)
gcd=i;
}
return gcd;
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-092 写函数求sin(x)的值。计算公式为：Sin(x)=X-X3/3!+X5/5!-X7/7!+ …+(-1)n-1X2n-1/(2n-1)!。', 6, 5, 'Function', '写函数求sin(x)的值。计算公式为：Sin(x)=X-X3/3!+X5/5!-X7/7!+ …+(-1)n-1X2n-1/(2n-1)!。', '#include
float fun(float,int);
float sin(int,float);
float term(int);
void main()
{
int n;
float x;
printf("请输入n,x值:\\n");
scanf("%d",&n);
scanf("%f",&x);
printf("sin(%f)=%f",x,sin(n,x));
}
float sin(int n,float x)
{
int i;
float s=0.0;
for(i=1;i<=n;i++)
s+=fun(-1.0,i-1)*fun(x,2*i-1)*term(2*i-1);
return s;
}
float fun(float x,int n)
{
int i;
float p=1.0;
for(i=0;i
p=p*x;
return p;
}
float term(int n)
{
int i;
float q=1.0;
for(i=1;i<=n;i++)
q/=i;
return q;
}', 1, '#include
float fun(float,int);
float sin(int,float);
float term(int);
void main()
{
int n;
float x;
printf("请输入n,x值:\\n");
scanf("%d",&n);
scanf("%f",&x);
printf("sin(%f)=%f",x,sin(n,x));
}
float sin(int n,float x)
{
int i;
float s=0.0;
for(i=1;i<=n;i++)
s+=fun(-1.0,i-1)*fun(x,2*i-1)*term(2*i-1);
return s;
}
float fun(float x,int n)
{
int i;
float p=1.0;
for(i=0;i
p=p*x;
return p;
}
float term(int n)
{
int i;
float q=1.0;
for(i=1;i<=n;i++)
q/=i;
return q;
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-093 编一函数使用冒泡法对若干个整数按从小到大的顺序排序，主函数中输入若干个整数到一个一维数组中，调用排序函数，将其排序，最后将原数组和排好序的数组输出。', 6, 5, 'Array', '编一函数使用冒泡法对若干个整数按从小到大的顺序排序，主函数中输入若干个整数到一个一维数组中，调用排序函数，将其排序，最后将原数组和排好序的数组输出。', '#include
#define N 10
void main()
{
int a[N],i,j,temp;
printf("请输入10个整数:\\n");
for(i=0;i
scanf("%d",&a);
printf("\\n");
printf("原始数据为:\\n");
for(i=0;i
printf("%d",a);
for(i=1;i<=N-1;i++)
{
for(j=0;j<=N-i-1;j++)
if(a[j]>a[j+1])
{
temp=a[j];
a[j]=a[j+1];
a[j+1]=temp;
}
}
printf("\\n排序后的数据为:\\n");
for(i=0;i
printf("%d",a);
printf("\\n");
}', 1, '#include
#define N 10
void main()
{
int a[N],i,j,temp;
printf("请输入10个整数:\\n");
for(i=0;i
scanf("%d",&a);
printf("\\n");
printf("原始数据为:\\n");
for(i=0;i
printf("%d",a);
for(i=1;i<=N-1;i++)
{
for(j=0;j<=N-i-1;j++)
if(a[j]>a[j+1])
{
temp=a[j];
a[j]=a[j+1];
a[j+1]=temp;
}
}
printf("\\n排序后的数据为:\\n");
for(i=0;i
printf("%d",a);
printf("\\n");
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-094 求一个m行n列的二维数组中的这样一个原素；它在它所在的行为最大，在它所在的列为最小。', 6, 5, 'Array', '求一个m行n列的二维数组中的这样一个原素；它在它所在的行为最大，在它所在的列为最小。', '#include
#define N 4
#define M 5
void main()
{
int i,j,k,a[N][M],max,maxj,flag;
printf("please ｉｎｐｕｔ matrix:\\n");
for(i=0;i
for(j=0;j
scanf("%d",&a[j]);
for(i=0;i
{
max=a[0];
maxj=0;
for(j=0;j
if(a[j]>max)
{
max=a[j];
maxj=j;
}
flag=1;
for(k=0;k
if(max>a[k][maxj])
{
flag=0;
continue;
}
if(flag)
{
printf("a[%d][%d]=%d\\n",i,maxj,max);
break;
}
}
if(! flag)
printf("It is not exist!\\n");
}', 1, '#include
#define N 4
#define M 5
void main()
{
int i,j,k,a[N][M],max,maxj,flag;
printf("please ｉｎｐｕｔ matrix:\\n");
for(i=0;i
for(j=0;j
scanf("%d",&a[j]);
for(i=0;i
{
max=a[0];
maxj=0;
for(j=0;j
if(a[j]>max)
{
max=a[j];
maxj=j;
}
flag=1;
for(k=0;k
if(max>a[k][maxj])
{
flag=0;
continue;
}
if(flag)
{
printf("a[%d][%d]=%d\\n",i,maxj,max);
break;
}
}
if(! flag)
printf("It is not exist!\\n");
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-095 编写一个函数求给定字符串长度，主函数中输入一个字符串，调用该子函数，求出该字符串的长度，输出。', 6, 5, 'String', '编写一个函数求给定字符串长度，主函数中输入一个字符串，调用该子函数，求出该字符串的长度，输出。', '#include
void main()
{
int length(char*p);
int len;
char str[20];
printf("ｉｎｐｕｔ string:");
scanf("%s",str);
len=length(str);
printf("The length of string is %d.\\n",len);
}
int length(char*p)
{
int n;
n=0;
while(*p!=''\\0'')
{
n++;
p++;
}
return(n);
}', 1, '#include
void main()
{
int length(char*p);
int len;
char str[20];
printf("ｉｎｐｕｔ string:");
scanf("%s",str);
len=length(str);
printf("The length of string is %d.\\n",len);
}
int length(char*p)
{
int n;
n=0;
while(*p!=''\\0'')
{
n++;
p++;
}
return(n);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-096 编写一个函数将给定字符串复制到另一个字符串中去，主函数中输入一个字符串，调用该子函数，复制出另一字符串，将两个串输出。', 6, 5, 'String', '编写一个函数将给定字符串复制到另一个字符串中去，主函数中输入一个字符串，调用该子函数，复制出另一字符串，将两个串输出。', '#include
#include
void main()
{
void copystr(char*,char*,int);
int m;
char str1[20],str2[20];
printf("\\nｉｎｐｕｔ string:");
gets(str1);
printf("which character that begin to copy?");
scanf("%d",&m);
if(strlen(str1)
printf("ｉｎｐｕｔ error!");
else
{
copystr(str1,str2,m);
printf("result:%s\\n",str2);
}
}
void copystr(char*p1,char*p2,int m)
{
int n;
n=0;
while(n
{
n++;
p1++;
}
while(*p1!=''\\0'')
{
*p2=*p1;
p1++;
p2++;
}
*p2=''\\0'';
}', 1, '#include
#include
void main()
{
void copystr(char*,char*,int);
int m;
char str1[20],str2[20];
printf("\\nｉｎｐｕｔ string:");
gets(str1);
printf("which character that begin to copy?");
scanf("%d",&m);
if(strlen(str1)
printf("ｉｎｐｕｔ error!");
else
{
copystr(str1,str2,m);
printf("result:%s\\n",str2);
}
}
void copystr(char*p1,char*p2,int m)
{
int n;
n=0;
while(n
{
n++;
p1++;
}
while(*p1!=''\\0'')
{
*p2=*p1;
p1++;
p2++;
}
*p2=''\\0'';
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-097 写函数求Cos(x)=1+X2/2!-X4/4!+X6/6!- …+(-1)nX2n/(2n)!。', 6, 5, 'Function', '写函数求Cos(x)=1+X2/2!-X4/4!+X6/6!- …+(-1)nX2n/(2n)!。', '#include
float fun(float,int);
float cos(int,float);
float term(int);
void main()
{
int n;
float x;
printf("请输入n,x值:\\n");
scanf("%d",&n);
scanf("%f",&x);
printf("cos(%f)=%f",x,cos(n,x));
}
float cos(int n,float x)
{
int i;
float s=-1.0;
for(i=1;i<=n;i++)
s+=fun(-1.0,i-1)*fun(x,2*i)*term(2*i);
return s;
}
float fun(float x,int n)
{
int i;
float p=1.0;
for(i=0;i
p=p*x;
return p;
}
float term(int n)
{
int i;
float q=1.0;
for(i=1;i<=n;i++)
q/=i;
return q;
}', 1, '#include
float fun(float,int);
float cos(int,float);
float term(int);
void main()
{
int n;
float x;
printf("请输入n,x值:\\n");
scanf("%d",&n);
scanf("%f",&x);
printf("cos(%f)=%f",x,cos(n,x));
}
float cos(int n,float x)
{
int i;
float s=-1.0;
for(i=1;i<=n;i++)
s+=fun(-1.0,i-1)*fun(x,2*i)*term(2*i);
return s;
}
float fun(float x,int n)
{
int i;
float p=1.0;
for(i=0;i
p=p*x;
return p;
}
float term(int n)
{
int i;
float q=1.0;
for(i=1;i<=n;i++)
q/=i;
return q;
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-098 编写一个函数将给定字符串中的大写字母转换成小写字母，主函数中输入一个字符串，调用该子函数，进行转换，将原字符串及转换后的字符串输出。', 6, 5, 'String', '编写一个函数将给定字符串中的大写字母转换成小写字母，主函数中输入一个字符串，调用该子函数，进行转换，将原字符串及转换后的字符串输出。', '#include
void main()
{
int i;
char a[11];
gets(a);
puts(a);
for(i=0;i<10;i++)
if(a>=65&&a<=90)
a=a+32;
puts(a);
}', 1, '#include
void main()
{
int i;
char a[11];
gets(a);
puts(a);
for(i=0;i<10;i++)
if(a>=65&&a<=90)
a=a+32;
puts(a);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-099 编写一个函数将给定的两个字符串连接成一个字符串：', 6, 5, 'String', '编写一个函数将给定的两个字符串连接成一个字符串：', '格式为：strcat(ch1,ch2)；
功能：将ch2复制到ch1的后面；
主函数中输入两个字符串，调用该子函数，求出连接之后的字符串，将两个原字符串及连接之后的结果串输出。
#include
#include
void main()
{
char str1[20],str2[8];
gets(str1);
gets(str2);
strcat(str1,str2);
printf("%s\\n",str1);
}', 1, '格式为：strcat(ch1,ch2)；
功能：将ch2复制到ch1的后面；
主函数中输入两个字符串，调用该子函数，求出连接之后的字符串，将两个原字符串及连接之后的结果串输出。
#include
#include
void main()
{
char str1[20],str2[8];
gets(str1);
gets(str2);
strcat(str1,str2);
printf("%s\\n",str1);
}', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
INSERT INTO qb_question(title, question_type, difficulty, chapter, stem, standard_answer, answer_format, analysis_text, analysis_source, status, created_by, created_at, updated_at, is_deleted) VALUES ('C100-100 用40元钱买苹果、西瓜和梨共100个，且三种水果都有。已知苹果0.4元一个，西瓜4元一个，梨0.2元一个。问可以买多少个？编程输出所有购买方案。', 6, 5, 'Array', '用40元钱买苹果、西瓜和梨共100个，且三种水果都有。已知苹果0.4元一个，西瓜4元一个，梨0.2元一个。问可以买多少个？编程输出所有购买方案。', '#include
void main()
{int i=1,j,k;
double r;
printf("苹果 西瓜 梨\\n");
while (i<=100)
{
j=1;
while(j<=(10-i))
{
k=100-i-j;
r=2*i/5+4*j+i/5;
if(r<=40)
printf("%d%7d%7d\\n",i,j,k);
j++;
}
i++;
}
}
101.编程：建立一个3×3的二维整数数组，求两条对角线上元素值得和，并将结果输出。（用函数调用方式编程）
102.编程计算：1*2*3+3*4*5+……+99*100*101的值。
#include
void main()
{
int i,j,k,sum=0;
for(i=1,j=2,k=3;i<100,j<101,k<102;i+=2,j+=2,k+=2)
sum=sum+i*j*k;
printf("%d\\n",sum);
}
103.有一个5×4的矩阵，编程实现：找出该矩阵中每行元素的最大值，并使该值成为该行的首列元素。
#include
void main ()
{
int a[5][4]={6,7,8,9,2,7,8,7,4,8,9,5,2,4,6,7,2,4,1,2},b[5][4]={6,7,8,9,2,7,8,7,4,8,9,5,2,4,6,7,2,4,1,2},t,i,j,k,p;
int max[5]={6,2,4,2,2};
for(i=0;i<5;i++)
{for(j=1;j<4;j++)
if(a[j]>max)
{
max=a[j];
}
}
for(k=0;k<5;k++)
{for(p=1;p<4;p++)
if(max[k]!=b[k][p])
{b[k][p]=b[k][p];}
else
{ t=b[k][0];
b[k][0]=b[k][p];
b[k][p]=t;
}
}
for(k=0;k<5;k++)
for(p=0;p<4;p++)
printf("\\nb[%d][%d]=%d",k,p,b[k][p]);
printf("\\n");
}
104.编写一个程序，使输入的一个字符串按反序存放在一字符数组中，然后输出。要求：
（1）在主调函数中输入字符串；
（2）写函数完成由主调函数传递来的字符串按反序存放；
（3）在主调函数中输出结果。
#include
#include
void main()
{
void inverse(char str[]);
char str[100];
printf("输入字符串：\\n");
gets(str);
inverse(str);
printf("%s\\n",str);
}
void inverse(char str[])
{char t;
int i,j;
for(i=0,j=strlen(str);i<(strlen(str)/2);i++,j--)
{t=str;
str=str[j-1];
str[j-1]=t;
}
}
105.从键盘输入五个字符串，分别求出字符串中长度最长和最短的字符串，请编程。（要求：不要使用strlen(_)函数编程）
106.输入10个整数，将其中最小的数与第一个数对换，把最大的数与最后一个数对换。请编程实现。
#include
#define N 10
void main()
{
int a[N],b[N],i,j,min,max,p,t;
printf("请输入数据\\n");
for(i=0;i
scanf("%d",&a);
for(i=0,j=0;i
b[j]=a;
for(j=0;j
printf("%3d",b[j]);
printf("\\n");
for(i=1;i
{
if(a[0]>a)
{a[0]=a;
min=a[0];}
}
for(i=0;i
{
if(a[N-1]
{a[N-1]=a;
max=a[N-1];}
}
for(j=0;j
{
if(min!=b[j])
{b[j]=b[j];}
else
{t=b[0];
b[0]=b[j];
b[j]=t;
}
}
for(j=0;j
{if(max!=b[j])
{b[j]=b[j];}
else
{p=b[N-1];
b[N-1]=b[j];
b[j]=p;
}
}
for(j=0;j
printf("%3d",b[j]);
printf("\\n");
}
107.写一个判断素数的函数，在主函数中调用素数的判断函数，求出2到1000之间的素数的累加和，将结果输出，请编程。
#include
#include
int isprime(int);
void main()
{
int i,sum=0;
for (i=2;i<=1000;i++)
if (isprime(i))
sum=sum+i;
printf("%d\\n",sum);
}
int isprime(int a)
{
int j;
for(j=2;j<=sqrt(a);j++)
if(a%j==0) return 0;
return 1;
}
108.编写一函数，由实参传来一个字符串，统计此字符串中字母、数字、空格和其他字符的个数，在主函数中输入字符串以及输出上述的结果。
#include
int letter,digit,space,others;
void main()
{
void count(char[]);
char text[80];
printf("输入字符串：\\n");
gets(text);
printf("字符串是:");
puts(text);
letter=0;
digit=0;
space=0;
others=0;
count(text);
printf("letter:%d,digit:%d,space:%d,others:%d\\n",letter,digit,space,others);
}
void count(char str[])
{int i;
for(i=0;str!=''\\0'';i++)
if((str>=''a''&&str<=''z'')||(str>=''A''&&str<=''Z''))
letter++;
else if(str>=''0''&&str<=''9'')
digit++;
else if(str==32)
space++;
else
others++;
}
109.请编程实现：将两个字符串s1和s2比较，如果s1>s2，输出一个正数；s1
要求：不要用strcpy函数，两个串用gets函数读入，输出的正数或负数的绝对值应是相比较的两个字符串相应字符的ASCⅡ码的差值。
#include
void main()
{
int i,resu;
char str1[100],str2[200];
printf("请输入str1:\\n");
gets(str1);
printf("请输入str2:\\n");
gets(str2);
i=0;
while((str1==str2)&&(str1!=''\\0'')) i++;
if(str1==''\\0''&&str2==''\\0'') resu=0;
else
resu=str1-str2;
printf("%d\\n",resu);
}
110.编写一个函数，由实参传来一个字符串，把串中所有大写字母变成相应的小写字母；原串中所有的小写字母变成相应的大写字母，在主函数中输入原字符串和输出变换后的字符串，请编程。
#include
void strupr(char str[]);
void main()
{
char text[20];
printf("请输入字符串：\\n");
gets(text);
printf("%s\\n",text);
strupr(text);
printf("%s\\n",text);
}
void strupr(char str[])
{
int i;
for(i=0;str!=''\\0'';i++)
if(str>=''a''&&str<=''z'')
str=str+''A''-''a'';
else if(str>=''A''&&str<=''Z'')
str=str-''A''+''a'';
}
111.编程实现：由键盘输入的任意一组字符中统计出大写字母的个m和小写字母的个数n，并输出m、n中的较大者。
#include
int fmax(int x,int y);
void main()
{
char str[80];
int m=0,n=0,i=0,k=0,c;
printf("请输入一个字符串\\n");
gets(str);
for(i=0;str!=''\\0'';i++)
if(''A''<=str&&str<=''Z'')
++m;
else if(''a''<=str&&str<=''z'')
++n;
else
++k;
printf("有%d个大写字母，有%d个小写字母：\\n",m,n);
c=fmax(m,n);
printf("max=%d\\n",c);
}
int fmax(int x,int y)
{
int z;
z=x>y?x:y;
return z;
}
112.定义一个含有30个整形元素的数组，按顺序分别赋予从2开始的偶数，然后按顺序每五个数求出一个平均值，放在另一个数组中并输出，请编程。
#include
void main()
{
int a[30] ,i,j=0,b[6][5],p,k;
int c[6];
for(i=0;i<30;i++)
a=2*(i+1);
for(i=0;i<30;i++)
{
printf("%3d",a);
j++;
if(j%5==0) printf("\\n");}
for(p=0;p<5;p++)
for(k=0;k<6;k++)
b[k][p]=a[5*k+p];
for(k=0;k<6;k++)
c[k]=(b[k][0]+b[k][1]+b[k][2]+b[k][3]+b[k][4])/5;
for(k=0;k<6;k++)
{printf("%3d",c[k]);}
printf("\\n");
}
113.输入一个整数，判断它能否被3，5，7整除，并输出以下信息之一：
（1）能同时被3，5，7整除；
（2）能被其中两数（要指出哪两个数）整除；
（3）能被其中一个数（要指出哪个数）整除。
#include
void main()
{
int a;
printf("请输入一个整数\\n");
scanf("%d",&a);
if(a%3==0&&a%5==0&&a%7==0)
printf("%d能同时被3，5，7整除\\n",a);
else if(a%3!=0&&a%5==0&&a%7==0)
printf("%d能同时被5，7整除\\n",a);
else if(a%3==0&&a%5!=0&&a%7==0)
printf("%d能同时被3,7整除\\n",a);
else if(a%3==0&&a%5==0&&a%7!=0)
printf("%d能同时被3,5整除\\n",a);
else if(a%3==0&&a%5!=0&&a%7!=0)
printf("%d能被3整除\\n",&a);
else if(a%3!=0&&a%5==0&&a%7!=0)
printf("%d能被5整除\\n",a);
else if(a%3!=0&&a%5!=0&&a%7==0)
printf("%d能被7整除\\n",a);
else printf("%d不能被3，5，7整除\\n",a);', 1, '#include
void main()
{int i=1,j,k;
double r;
printf("苹果 西瓜 梨\\n");
while (i<=100)
{
j=1;
while(j<=(10-i))
{
k=100-i-j;
r=2*i/5+4*j+i/5;
if(r<=40)
printf("%d%7d%7d\\n",i,j,k);
j++;
}
i++;
}
}
101.编程：建立一个3×3的二维整数数组，求两条对角线上元素值得和，并将结果输出。（用函数调用方式编程）
102.编程计算：1*2*3+3*4*5+……+99*100*101的值。
#include
void main()
{
int i,j,k,sum=0;
for(i=1,j=2,k=3;i<100,j<101,k<102;i+=2,j+=2,k+=2)
sum=sum+i*j*k;
printf("%d\\n",sum);
}
103.有一个5×4的矩阵，编程实现：找出该矩阵中每行元素的最大值，并使该值成为该行的首列元素。
#include
void main ()
{
int a[5][4]={6,7,8,9,2,7,8,7,4,8,9,5,2,4,6,7,2,4,1,2},b[5][4]={6,7,8,9,2,7,8,7,4,8,9,5,2,4,6,7,2,4,1,2},t,i,j,k,p;
int max[5]={6,2,4,2,2};
for(i=0;i<5;i++)
{for(j=1;j<4;j++)
if(a[j]>max)
{
max=a[j];
}
}
for(k=0;k<5;k++)
{for(p=1;p<4;p++)
if(max[k]!=b[k][p])
{b[k][p]=b[k][p];}
else
{ t=b[k][0];
b[k][0]=b[k][p];
b[k][p]=t;
}
}
for(k=0;k<5;k++)
for(p=0;p<4;p++)
printf("\\nb[%d][%d]=%d",k,p,b[k][p]);
printf("\\n");
}
104.编写一个程序，使输入的一个字符串按反序存放在一字符数组中，然后输出。要求：
（1）在主调函数中输入字符串；
（2）写函数完成由主调函数传递来的字符串按反序存放；
（3）在主调函数中输出结果。
#include
#include
void main()
{
void inverse(char str[]);
char str[100];
printf("输入字符串：\\n");
gets(str);
inverse(str);
printf("%s\\n",str);
}
void inverse(char str[])
{char t;
int i,j;
for(i=0,j=strlen(str);i<(strlen(str)/2);i++,j--)
{t=str;
str=str[j-1];
str[j-1]=t;
}
}
105.从键盘输入五个字符串，分别求出字符串中长度最长和最短的字符串，请编程。（要求：不要使用strlen(_)函数编程）
106.输入10个整数，将其中最小的数与第一个数对换，把最大的数与最后一个数对换。请编程实现。
#include
#define N 10
void main()
{
int a[N],b[N],i,j,min,max,p,t;
printf("请输入数据\\n");
for(i=0;i
scanf("%d",&a);
for(i=0,j=0;i
b[j]=a;
for(j=0;j
printf("%3d",b[j]);
printf("\\n");
for(i=1;i
{
if(a[0]>a)
{a[0]=a;
min=a[0];}
}
for(i=0;i
{
if(a[N-1]
{a[N-1]=a;
max=a[N-1];}
}
for(j=0;j
{
if(min!=b[j])
{b[j]=b[j];}
else
{t=b[0];
b[0]=b[j];
b[j]=t;
}
}
for(j=0;j
{if(max!=b[j])
{b[j]=b[j];}
else
{p=b[N-1];
b[N-1]=b[j];
b[j]=p;
}
}
for(j=0;j
printf("%3d",b[j]);
printf("\\n");
}
107.写一个判断素数的函数，在主函数中调用素数的判断函数，求出2到1000之间的素数的累加和，将结果输出，请编程。
#include
#include
int isprime(int);
void main()
{
int i,sum=0;
for (i=2;i<=1000;i++)
if (isprime(i))
sum=sum+i;
printf("%d\\n",sum);
}
int isprime(int a)
{
int j;
for(j=2;j<=sqrt(a);j++)
if(a%j==0) return 0;
return 1;
}
108.编写一函数，由实参传来一个字符串，统计此字符串中字母、数字、空格和其他字符的个数，在主函数中输入字符串以及输出上述的结果。
#include
int letter,digit,space,others;
void main()
{
void count(char[]);
char text[80];
printf("输入字符串：\\n");
gets(text);
printf("字符串是:");
puts(text);
letter=0;
digit=0;
space=0;
others=0;
count(text);
printf("letter:%d,digit:%d,space:%d,others:%d\\n",letter,digit,space,others);
}
void count(char str[])
{int i;
for(i=0;str!=''\\0'';i++)
if((str>=''a''&&str<=''z'')||(str>=''A''&&str<=''Z''))
letter++;
else if(str>=''0''&&str<=''9'')
digit++;
else if(str==32)
space++;
else
others++;
}
109.请编程实现：将两个字符串s1和s2比较，如果s1>s2，输出一个正数；s1
要求：不要用strcpy函数，两个串用gets函数读入，输出的正数或负数的绝对值应是相比较的两个字符串相应字符的ASCⅡ码的差值。
#include
void main()
{
int i,resu;
char str1[100],str2[200];
printf("请输入str1:\\n");
gets(str1);
printf("请输入str2:\\n");
gets(str2);
i=0;
while((str1==str2)&&(str1!=''\\0'')) i++;
if(str1==''\\0''&&str2==''\\0'') resu=0;
else
resu=str1-str2;
printf("%d\\n",resu);
}
110.编写一个函数，由实参传来一个字符串，把串中所有大写字母变成相应的小写字母；原串中所有的小写字母变成相应的大写字母，在主函数中输入原字符串和输出变换后的字符串，请编程。
#include
void strupr(char str[]);
void main()
{
char text[20];
printf("请输入字符串：\\n");
gets(text);
printf("%s\\n",text);
strupr(text);
printf("%s\\n",text);
}
void strupr(char str[])
{
int i;
for(i=0;str!=''\\0'';i++)
if(str>=''a''&&str<=''z'')
str=str+''A''-''a'';
else if(str>=''A''&&str<=''Z'')
str=str-''A''+''a'';
}
111.编程实现：由键盘输入的任意一组字符中统计出大写字母的个m和小写字母的个数n，并输出m、n中的较大者。
#include
int fmax(int x,int y);
void main()
{
char str[80];
int m=0,n=0,i=0,k=0,c;
printf("请输入一个字符串\\n");
gets(str);
for(i=0;str!=''\\0'';i++)
if(''A''<=str&&str<=''Z'')
++m;
else if(''a''<=str&&str<=''z'')
++n;
else
++k;
printf("有%d个大写字母，有%d个小写字母：\\n",m,n);
c=fmax(m,n);
printf("max=%d\\n",c);
}
int fmax(int x,int y)
{
int z;
z=x>y?x:y;
return z;
}
112.定义一个含有30个整形元素的数组，按顺序分别赋予从2开始的偶数，然后按顺序每五个数求出一个平均值，放在另一个数组中并输出，请编程。
#include
void main()
{
int a[30] ,i,j=0,b[6][5],p,k;
int c[6];
for(i=0;i<30;i++)
a=2*(i+1);
for(i=0;i<30;i++)
{
printf("%3d",a);
j++;
if(j%5==0) printf("\\n");}
for(p=0;p<5;p++)
for(k=0;k<6;k++)
b[k][p]=a[5*k+p];
for(k=0;k<6;k++)
c[k]=(b[k][0]+b[k][1]+b[k][2]+b[k][3]+b[k][4])/5;
for(k=0;k<6;k++)
{printf("%3d",c[k]);}
printf("\\n");
}
113.输入一个整数，判断它能否被3，5，7整除，并输出以下信息之一：
（1）能同时被3，5，7整除；
（2）能被其中两数（要指出哪两个数）整除；
（3）能被其中一个数（要指出哪个数）整除。
#include
void main()
{
int a;
printf("请输入一个整数\\n");
scanf("%d",&a);
if(a%3==0&&a%5==0&&a%7==0)
printf("%d能同时被3，5，7整除\\n",a);
else if(a%3!=0&&a%5==0&&a%7==0)
printf("%d能同时被5，7整除\\n",a);
else if(a%3==0&&a%5!=0&&a%7==0)
printf("%d能同时被3,7整除\\n",a);
else if(a%3==0&&a%5==0&&a%7!=0)
printf("%d能同时被3,5整除\\n",a);
else if(a%3==0&&a%5!=0&&a%7!=0)
printf("%d能被3整除\\n",&a);
else if(a%3!=0&&a%5==0&&a%7!=0)
printf("%d能被5整除\\n",a);
else if(a%3!=0&&a%5!=0&&a%7==0)
printf("%d能被7整除\\n",a);
else printf("%d不能被3，5，7整除\\n",a);', 1, 2, (SELECT id FROM sys_user WHERE username='admin' LIMIT 1), NOW(3), NOW(3), 0);
-- tag master data
INSERT INTO qb_tag(tag_name, tag_code, parent_id, tag_level, tag_type, sort_order, created_at, updated_at, is_deleted)
SELECT '题库-C100', 'C100_BANK', NULL, 1, 3, 100, NOW(3), NOW(3), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM qb_tag WHERE tag_name='题库-C100' AND is_deleted=0);
INSERT INTO qb_tag(tag_name, tag_code, parent_id, tag_level, tag_type, sort_order, created_at, updated_at, is_deleted)
SELECT '章节-C100', 'C100_CHAPTER', NULL, 1, 2, 110, NOW(3), NOW(3), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM qb_tag WHERE tag_name='章节-C100' AND is_deleted=0);
INSERT INTO qb_tag(tag_name, tag_code, parent_id, tag_level, tag_type, sort_order, created_at, updated_at, is_deleted)
SELECT '难度-C100', 'C100_DIFF', NULL, 1, 1, 120, NOW(3), NOW(3), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM qb_tag WHERE tag_name='难度-C100' AND is_deleted=0);
INSERT INTO qb_tag(tag_name, tag_code, parent_id, tag_level, tag_type, sort_order, created_at, updated_at, is_deleted)
SELECT 'C100-基础', 'C100_CH_BASIC', (SELECT id FROM qb_tag WHERE tag_name='章节-C100' AND is_deleted=0 LIMIT 1), 2, 2, 1, NOW(3), NOW(3), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM qb_tag WHERE tag_name='C100-基础' AND is_deleted=0);
INSERT INTO qb_tag(tag_name, tag_code, parent_id, tag_level, tag_type, sort_order, created_at, updated_at, is_deleted)
SELECT 'C100-字符串', 'C100_CH_STRING', (SELECT id FROM qb_tag WHERE tag_name='章节-C100' AND is_deleted=0 LIMIT 1), 2, 2, 2, NOW(3), NOW(3), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM qb_tag WHERE tag_name='C100-字符串' AND is_deleted=0);
INSERT INTO qb_tag(tag_name, tag_code, parent_id, tag_level, tag_type, sort_order, created_at, updated_at, is_deleted)
SELECT 'C100-数组', 'C100_CH_ARRAY', (SELECT id FROM qb_tag WHERE tag_name='章节-C100' AND is_deleted=0 LIMIT 1), 2, 2, 3, NOW(3), NOW(3), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM qb_tag WHERE tag_name='C100-数组' AND is_deleted=0);
INSERT INTO qb_tag(tag_name, tag_code, parent_id, tag_level, tag_type, sort_order, created_at, updated_at, is_deleted)
SELECT 'C100-函数与递归', 'C100_CH_FUNCTION', (SELECT id FROM qb_tag WHERE tag_name='章节-C100' AND is_deleted=0 LIMIT 1), 2, 2, 4, NOW(3), NOW(3), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM qb_tag WHERE tag_name='C100-函数与递归' AND is_deleted=0);
INSERT INTO qb_tag(tag_name, tag_code, parent_id, tag_level, tag_type, sort_order, created_at, updated_at, is_deleted)
SELECT 'C100-指针', 'C100_CH_POINTER', (SELECT id FROM qb_tag WHERE tag_name='章节-C100' AND is_deleted=0 LIMIT 1), 2, 2, 5, NOW(3), NOW(3), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM qb_tag WHERE tag_name='C100-指针' AND is_deleted=0);
INSERT INTO qb_tag(tag_name, tag_code, parent_id, tag_level, tag_type, sort_order, created_at, updated_at, is_deleted)
SELECT 'C100-数据结构', 'C100_CH_DATASTRUCTURE', (SELECT id FROM qb_tag WHERE tag_name='章节-C100' AND is_deleted=0 LIMIT 1), 2, 2, 6, NOW(3), NOW(3), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM qb_tag WHERE tag_name='C100-数据结构' AND is_deleted=0);
INSERT INTO qb_tag(tag_name, tag_code, parent_id, tag_level, tag_type, sort_order, created_at, updated_at, is_deleted)
SELECT 'C100-文件', 'C100_CH_FILEIO', (SELECT id FROM qb_tag WHERE tag_name='章节-C100' AND is_deleted=0 LIMIT 1), 2, 2, 7, NOW(3), NOW(3), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM qb_tag WHERE tag_name='C100-文件' AND is_deleted=0);
INSERT INTO qb_tag(tag_name, tag_code, parent_id, tag_level, tag_type, sort_order, created_at, updated_at, is_deleted)
SELECT 'C100-难度1', 'C100_DIFF_1', (SELECT id FROM qb_tag WHERE tag_name='难度-C100' AND is_deleted=0 LIMIT 1), 2, 1, 1, NOW(3), NOW(3), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM qb_tag WHERE tag_name='C100-难度1' AND is_deleted=0);
INSERT INTO qb_tag(tag_name, tag_code, parent_id, tag_level, tag_type, sort_order, created_at, updated_at, is_deleted)
SELECT 'C100-难度2', 'C100_DIFF_2', (SELECT id FROM qb_tag WHERE tag_name='难度-C100' AND is_deleted=0 LIMIT 1), 2, 1, 2, NOW(3), NOW(3), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM qb_tag WHERE tag_name='C100-难度2' AND is_deleted=0);
INSERT INTO qb_tag(tag_name, tag_code, parent_id, tag_level, tag_type, sort_order, created_at, updated_at, is_deleted)
SELECT 'C100-难度3', 'C100_DIFF_3', (SELECT id FROM qb_tag WHERE tag_name='难度-C100' AND is_deleted=0 LIMIT 1), 2, 1, 3, NOW(3), NOW(3), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM qb_tag WHERE tag_name='C100-难度3' AND is_deleted=0);
INSERT INTO qb_tag(tag_name, tag_code, parent_id, tag_level, tag_type, sort_order, created_at, updated_at, is_deleted)
SELECT 'C100-难度4', 'C100_DIFF_4', (SELECT id FROM qb_tag WHERE tag_name='难度-C100' AND is_deleted=0 LIMIT 1), 2, 1, 4, NOW(3), NOW(3), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM qb_tag WHERE tag_name='C100-难度4' AND is_deleted=0);
INSERT INTO qb_tag(tag_name, tag_code, parent_id, tag_level, tag_type, sort_order, created_at, updated_at, is_deleted)
SELECT 'C100-难度5', 'C100_DIFF_5', (SELECT id FROM qb_tag WHERE tag_name='难度-C100' AND is_deleted=0 LIMIT 1), 2, 1, 5, NOW(3), NOW(3), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM qb_tag WHERE tag_name='C100-难度5' AND is_deleted=0);
-- relation: all C100 questions -> 题库-C100
INSERT INTO qb_question_tag(question_id, tag_id, created_at)
SELECT q.id, t.id, NOW(3)
FROM qb_question q
JOIN qb_tag t ON t.tag_name='题库-C100' AND t.is_deleted=0
LEFT JOIN qb_question_tag qt ON qt.question_id=q.id AND qt.tag_id=t.id
WHERE q.title LIKE 'C100-%' AND q.is_deleted=0 AND qt.question_id IS NULL;
-- relation: chapter tag
INSERT INTO qb_question_tag(question_id, tag_id, created_at)
SELECT q.id, t.id, NOW(3)
FROM qb_question q
JOIN qb_tag t ON t.tag_name = (
  CASE q.chapter
    WHEN 'Basic' THEN 'C100-基础'
    WHEN 'String' THEN 'C100-字符串'
    WHEN 'Array' THEN 'C100-数组'
    WHEN 'Function' THEN 'C100-函数与递归'
    WHEN 'Pointer' THEN 'C100-指针'
    WHEN 'DataStructure' THEN 'C100-数据结构'
    WHEN 'FileIO' THEN 'C100-文件'
    ELSE 'C100-基础'
  END
) AND t.is_deleted=0
LEFT JOIN qb_question_tag qt ON qt.question_id=q.id AND qt.tag_id=t.id
WHERE q.title LIKE 'C100-%' AND q.is_deleted=0 AND qt.question_id IS NULL;
-- relation: difficulty tag
INSERT INTO qb_question_tag(question_id, tag_id, created_at)
SELECT q.id, t.id, NOW(3)
FROM qb_question q
JOIN qb_tag t ON t.tag_name = CONCAT('C100-难度', q.difficulty) AND t.is_deleted=0
LEFT JOIN qb_question_tag qt ON qt.question_id=q.id AND qt.tag_id=t.id
WHERE q.title LIKE 'C100-%' AND q.is_deleted=0 AND qt.question_id IS NULL;
COMMIT;
