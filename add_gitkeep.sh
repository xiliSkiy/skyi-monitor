#!/bin/bash

# 找到所有空目录并排除.git目录
EMPTY_DIRS=$(find /Users/liangxin/Downloads/code/skyi -type d -empty | grep -v "\.git" | grep -v "target")

# 在每个空目录中创建.gitkeep文件
for dir in $EMPTY_DIRS; do
  touch "$dir/.gitkeep"
  echo "已在 $dir 中创建 .gitkeep 文件"
done

echo "完成! 共处理了 $(echo "$EMPTY_DIRS" | wc -l | tr -d ' ') 个空目录" 