// 测试ProxyAI Web服务器API的脚本
const http = require('http');

const BASE_URL = 'http://localhost:8090';

// 测试函数
async function testAPI() {
    console.log('🧪 开始测试ProxyAI Web服务器API...\n');
    
    try {
        // 测试1: 获取所有指标数据
        console.log('📊 测试1: 获取所有指标数据');
        const metricsData = await makeRequest('/api/metrics');
        console.log('✅ 响应状态:', metricsData.status);
        console.log('📋 数据类型:', Array.isArray(metricsData.data) ? '数组' : '对象');
        console.log('🔢 数据长度:', Array.isArray(metricsData.data) ? metricsData.data.length : 'N/A');
        console.log('📄 数据示例:', JSON.stringify(metricsData.data.slice ? metricsData.data.slice(0, 2) : metricsData.data, null, 2));
        console.log('');
        
        // 测试2: 获取数据摘要
        console.log('📈 测试2: 获取数据摘要');
        const summaryData = await makeRequest('/api/metrics/summary');
        console.log('✅ 响应状态:', summaryData.status);
        console.log('📋 数据类型:', Array.isArray(summaryData.data) ? '数组' : '对象');
        console.log('📄 数据内容:', JSON.stringify(summaryData.data, null, 2));
        console.log('');
        
        // 测试3: 获取操作类型
        console.log('🔧 测试3: 获取操作类型');
        const actionData = await makeRequest('/api/metrics/actions');
        console.log('✅ 响应状态:', actionData.status);
        console.log('📋 数据类型:', Array.isArray(actionData.data) ? '数组' : '对象');
        console.log('📄 数据内容:', JSON.stringify(actionData.data, null, 2));
        console.log('');
        
        // 测试4: 获取模型名称
        console.log('🤖 测试4: 获取模型名称');
        const modelData = await makeRequest('/api/metrics/models');
        console.log('✅ 响应状态:', modelData.status);
        console.log('📋 数据类型:', Array.isArray(modelData.data) ? '数组' : '对象');
        console.log('📄 数据内容:', JSON.stringify(modelData.data, null, 2));
        console.log('');
        
        console.log('🎉 所有API测试完成！');
        
    } catch (error) {
        console.error('❌ 测试过程中发生错误:', error.message);
    }
}

// 发送HTTP请求的辅助函数
function makeRequest(path) {
    return new Promise((resolve, reject) => {
        const options = {
            hostname: 'localhost',
            port: 8090,
            path: path,
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        };
        
        const req = http.request(options, (res) => {
            let data = '';
            
            res.on('data', (chunk) => {
                data += chunk;
            });
            
            res.on('end', () => {
                try {
                    const jsonData = JSON.parse(data);
                    resolve({
                        status: res.statusCode,
                        data: jsonData
                    });
                } catch (parseError) {
                    resolve({
                        status: res.statusCode,
                        data: data,
                        parseError: parseError.message
                    });
                }
            });
        });
        
        req.on('error', (error) => {
            reject(error);
        });
        
        req.setTimeout(5000, () => {
            req.destroy();
            reject(new Error('请求超时'));
        });
        
        req.end();
    });
}

// 检查服务器是否运行
async function checkServerStatus() {
    try {
        const response = await makeRequest('/');
        console.log('🌐 服务器状态: 运行中');
        console.log('📡 响应状态:', response.status);
        return true;
    } catch (error) {
        console.log('❌ 服务器未运行或无法访问');
        console.log('💡 请确保ProxyAI插件已启动，Web服务器在端口8090上运行');
        return false;
    }
}

// 主函数
async function main() {
    console.log('🚀 ProxyAI Web服务器API测试工具\n');
    
    // 检查服务器状态
    const serverRunning = await checkServerStatus();
    if (!serverRunning) {
        return;
    }
    
    console.log('');
    
    // 运行API测试
    await testAPI();
}

// 运行测试
main().catch(console.error);
