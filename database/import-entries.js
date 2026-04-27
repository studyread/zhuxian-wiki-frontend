/**
 * 诛仙世界 Wiki 知识库导入脚本
 * 使用方法: node import-entries.js
 * 
 * 依赖: mysql2 (已在项目中安装)
 */

const fs = require('fs');
const path = require('path');
const mysql = require('mysql2/promise');

// ==================== 配置 ====================
const dbConfig = {
  host: 'localhost',
  user: 'root',
  password: '123456',
  database: 'zhuxian_wiki',
  charset: 'utf8mb4'
};

const sqlFile = path.join(__dirname, 'crawl_entries_import.sql');

// ==================== 主流程 ====================
async function main() {
  console.log('========================================');
  console.log('诛仙世界 Wiki 知识库导入脚本');
  console.log('========================================\n');

  // 检查SQL文件
  if (!fs.existsSync(sqlFile)) {
    console.error('[错误] SQL文件不存在:', sqlFile);
    process.exit(1);
  }

  // 读取SQL文件
  console.log('[1/3] 读取SQL文件...');
  const sqlContent = fs.readFileSync(sqlFile, 'utf8');
  console.log('      文件大小:', sqlContent.length, '字符');

  // 连接数据库
  console.log('[2/3] 连接数据库...');
  let connection;
  try {
    connection = await mysql.createConnection(dbConfig);
    await connection.query(`SET NAMES utf8mb4`);
    console.log('      数据库连接成功');
  } catch (err) {
    console.error('      [错误] 数据库连接失败:', err.message);
    process.exit(1);
  }

  // 执行SQL
  console.log('[3/3] 执行导入...');
  try {
    // 分割多个INSERT语句并执行
    const statements = sqlContent
      .split(/;[\s\r\n]+INSERT INTO/i)
      .filter(s => s.trim().length > 0);

    let insertedCount = 0;
    for (let i = 0; i < statements.length; i++) {
      let sql = statements[i].trim();
      // 第一个语句可能以 INSERT INTO 开头，其他的需要加上
      if (i > 0 && !sql.toUpperCase().startsWith('INSERT INTO')) {
        sql = 'INSERT INTO ' + sql;
      }
      // 只执行INSERT语句，跳过注释和SELECT
      if (sql.toUpperCase().includes('INSERT INTO') && !sql.includes('SELECT')) {
        try {
          await connection.query(sql);
          // 计算插入的行数（粗略估算）
          const matches = sql.match(/VALUES\s*\(/gi);
          if (matches) {
            // 统计VALUES后面的括号对数
            const valueBlocks = sql.split(/VALUES\s*\(/i);
            if (valueBlocks.length > 1) {
              // 计算右括号数量来估算行数
              const parenCount = (valueBlocks[1].match(/\(/g) || []).length;
              insertedCount += Math.max(1, parenCount);
            }
          }
        } catch (err) {
          console.log('      警告:', err.message.substring(0, 100));
        }
      }
    }
    console.log('      导入完成!');
  } catch (err) {
    console.error('      [错误] SQL执行失败:', err.message);
  }

  // 验证结果
  console.log('\n验证导入结果...');
  try {
    const [rows] = await connection.query('SELECT COUNT(*) as total FROM knowledge_entry');
    console.log('      当前知识库词条总数:', rows[0].total);
  } catch (err) {
    console.log('      无法获取统计信息');
  }

  await connection.end();

  console.log('\n========================================');
  console.log('导入完成!');
  console.log('========================================');
}

main().catch(err => {
  console.error('执行错误:', err);
  process.exit(1);
});
