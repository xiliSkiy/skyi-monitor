<template>
  <div class="task-management">
    <el-container>
      <el-header height="auto">
        <div class="page-header">
          <div class="title-section">
            <h2>采集任务管理</h2>
            <span class="subtitle">配置和管理数据采集任务</span>
          </div>
          <div class="action-section">
            <el-button type="primary" @click="addTask">新建任务</el-button>
            <el-button type="success" @click="batchExecute" :disabled="!hasSelectedTasks">
              <el-icon><CaretRight /></el-icon>批量执行
            </el-button>
            <el-button type="danger" @click="batchDelete" :disabled="!hasSelectedTasks">
              <el-icon><Delete /></el-icon>批量删除
            </el-button>
          </div>
        </div>
      </el-header>
      
      <el-container>
        <el-main>
          <!-- 任务列表组件 -->
          <task-list
            v-if="!showTaskForm"
            :tasks="tasks"
            :loading="loading"
            @add="addTask"
            @edit="editTask"
            @view="viewTask"
            @execute="executeTask"
            @cancel="cancelTask"
            @delete="deleteTask"
            @selection-change="handleSelectionChange"
          />
          
          <!-- 任务表单组件 -->
          <el-card v-if="showTaskForm" class="form-card">
            <template #header>
              <div class="form-header">
                <h3>{{ formTitle }}</h3>
                <el-button v-if="viewMode" type="primary" @click="editCurrentTask">编辑</el-button>
              </div>
            </template>
            
            <task-form
              v-if="!viewMode"
              :task-data="currentTask"
              :loading="submitting"
              @submit="submitTask"
              @cancel="cancelEdit"
            />
            
            <!-- 查看模式下的任务详情 -->
            <div v-else class="task-view">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="任务名称">{{ currentTask.title }}</el-descriptions-item>
                <el-descriptions-item label="采集目标">{{ currentTask.target }}</el-descriptions-item>
                <el-descriptions-item label="采集类型">{{ currentTask.type }}</el-descriptions-item>
                <el-descriptions-item label="任务状态">
                  <el-tag :type="getStatusType(currentTask.status)">{{ getStatusText(currentTask.status) }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ formatDateTime(currentTask.createTime) }}</el-descriptions-item>
                <el-descriptions-item label="最后执行时间">{{ formatDateTime(currentTask.lastExecuteTime) }}</el-descriptions-item>
                <el-descriptions-item label="任务描述" :span="2">{{ currentTask.description }}</el-descriptions-item>
                
                <!-- 根据不同任务类型显示不同的详情 -->
                <template v-if="currentTask.type === 'SNMP'">
                  <el-descriptions-item label="SNMP版本">v{{ currentTask.snmpVersion }}</el-descriptions-item>
                  <el-descriptions-item label="OID列表" :span="2">
                    <el-tag v-for="(oid, index) in currentTask.oids" :key="index" style="margin-right: 5px; margin-bottom: 5px;">
                      {{ oid }}
                    </el-tag>
                  </el-descriptions-item>
                </template>
                
                <template v-if="currentTask.type === 'API'">
                  <el-descriptions-item label="HTTP方法">{{ currentTask.httpMethod }}</el-descriptions-item>
                  <el-descriptions-item label="URL">{{ currentTask.target }}</el-descriptions-item>
                  <el-descriptions-item label="JsonPath">{{ currentTask.jsonPath }}</el-descriptions-item>
                </template>
                
                <el-descriptions-item label="标签" :span="2">
                  <el-tag v-for="tag in currentTask.tags" :key="tag" style="margin-right: 5px;">
                    {{ tag }}
                  </el-tag>
                </el-descriptions-item>
                
                <el-descriptions-item label="调度方式" :span="2">
                  <template v-if="currentTask.scheduleType === 'once'">
                    单次执行: {{ formatDateTime(currentTask.executeTime) }}
                  </template>
                  <template v-else-if="currentTask.scheduleType === 'interval'">
                    定时执行: 每{{ currentTask.interval }}{{ getIntervalUnitText(currentTask.intervalUnit) }}
                  </template>
                  <template v-else-if="currentTask.scheduleType === 'cron'">
                    Cron表达式: {{ currentTask.cronExpression }}
                  </template>
                </el-descriptions-item>
              </el-descriptions>
              
              <!-- 任务执行历史记录 -->
              <div class="history-section">
                <h4>执行历史</h4>
                <el-table
                  :data="taskHistory"
                  style="width: 100%"
                  :max-height="300"
                  v-loading="loadingHistory"
                >
                  <el-table-column prop="executeTime" label="执行时间" width="180">
                    <template #default="scope">
                      {{ formatDateTime(scope.row.executeTime) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="status" label="执行状态" width="100">
                    <template #default="scope">
                      <el-tag :type="getExecutionStatusType(scope.row.status)">
                        {{ getExecutionStatusText(scope.row.status) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="duration" label="耗时" width="100">
                    <template #default="scope">
                      {{ scope.row.duration }}ms
                    </template>
                  </el-table-column>
                  <el-table-column prop="result" label="执行结果">
                    <template #default="scope">
                      <el-button type="primary" link @click="viewExecutionDetail(scope.row)">
                        查看详情
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-card>
        </el-main>
      </el-container>
    </el-container>
    
    <!-- 执行结果详情对话框 -->
    <el-dialog
      v-model="executionDetailVisible"
      title="执行结果详情"
      width="70%"
    >
      <div v-if="currentExecution">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="执行时间">{{ formatDateTime(currentExecution.executeTime) }}</el-descriptions-item>
          <el-descriptions-item label="执行状态">
            <el-tag :type="getExecutionStatusType(currentExecution.status)">
              {{ getExecutionStatusText(currentExecution.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="耗时">{{ currentExecution.duration }}ms</el-descriptions-item>
          <el-descriptions-item label="执行节点">{{ currentExecution.node || '本地' }}</el-descriptions-item>
        </el-descriptions>
        
        <el-tabs v-model="executionDetailTab" class="execution-tabs">
          <el-tab-pane label="结果数据" name="data">
            <pre class="result-data">{{ formatResultData(currentExecution.resultData) }}</pre>
          </el-tab-pane>
          <el-tab-pane label="错误信息" name="error" v-if="currentExecution.errorMessage">
            <pre class="error-message">{{ currentExecution.errorMessage }}</pre>
          </el-tab-pane>
          <el-tab-pane label="原始输出" name="raw">
            <pre class="raw-output">{{ currentExecution.rawOutput }}</pre>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
    
    <!-- 批量操作确认对话框 -->
    <el-dialog
      v-model="batchConfirmVisible"
      :title="batchActionTitle"
      width="30%"
    >
      <span>{{ batchConfirmMessage }}</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchConfirmVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmBatchAction" :loading="batchActionLoading">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, reactive } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import TaskList from '@/components/collector/TaskList.vue';
import TaskForm from '@/components/collector/TaskForm.vue';
import { CaretRight, Delete } from '@element-plus/icons-vue';
import type { TaskType, TaskStatus, ExecutionStatus, IntervalUnit, Task, TaskExecution } from '@/types/collector';

// 页面状态
const loading = ref(false);
const submitting = ref(false);
const showTaskForm = ref(false);
const viewMode = ref(false);
const formTitle = ref('');

// 任务相关数据
const tasks = ref<Task[]>([]);
const currentTask = ref<Task>({} as Task);
const selectedTasks = ref<Task[]>([]);
const hasSelectedTasks = computed(() => selectedTasks.value.length > 0);

// 任务执行历史
const taskHistory = ref<TaskExecution[]>([]);
const loadingHistory = ref(false);
const currentExecution = ref<TaskExecution | null>(null);
const executionDetailVisible = ref(false);
const executionDetailTab = ref('data');

// 批量操作
const batchConfirmVisible = ref(false);
const batchActionType = ref('');
const batchActionLoading = ref(false);
const batchActionTitle = computed(() => batchActionType.value === 'execute' ? '批量执行任务' : '批量删除任务');
const batchConfirmMessage = computed(() => {
  return batchActionType.value === 'execute'
    ? `确认要执行选中的${selectedTasks.value.length}个任务吗？`
    : `确认要删除选中的${selectedTasks.value.length}个任务吗？此操作不可恢复。`;
});

// 加载任务列表
const loadTasks = async () => {
  loading.value = true;
  try {
    // TODO: 实际项目中应该调用API接口获取任务列表
    // const response = await api.getTasks();
    // tasks.value = response.data;
    
    // 模拟数据
    setTimeout(() => {
      tasks.value = [
        {
          id: '1',
          title: 'CPU使用率监控',
          description: '通过SNMP采集设备CPU使用率数据',
          target: '192.168.1.100',
          port: '161',
          type: 'SNMP' as TaskType,
          status: 'RUNNING' as TaskStatus,
          snmpVersion: '2c',
          community: 'public',
          oids: ['.1.3.6.1.2.1.25.3.3.1.2'],
          tags: ['服务器', 'CPU'],
          scheduleType: 'interval',
          interval: 5,
          intervalUnit: 'MINUTES' as IntervalUnit,
          createTime: new Date().getTime() - 86400000,
          lastExecuteTime: new Date().getTime() - 3600000
        },
        {
          id: '2',
          title: '内存使用情况',
          description: '通过SNMP采集设备内存使用情况',
          target: '192.168.1.100',
          port: '161',
          type: 'SNMP' as TaskType,
          status: 'PAUSED' as TaskStatus,
          snmpVersion: '2c',
          community: 'public',
          oids: ['.1.3.6.1.2.1.25.2.3.1.5', '.1.3.6.1.2.1.25.2.3.1.6'],
          tags: ['服务器', '内存'],
          scheduleType: 'interval',
          interval: 10,
          intervalUnit: 'MINUTES' as IntervalUnit,
          createTime: new Date().getTime() - 172800000,
          lastExecuteTime: new Date().getTime() - 7200000
        },
        {
          id: '3',
          title: '气象数据采集',
          description: '通过API采集天气数据',
          target: 'https://api.weather.com/v1/current',
          type: 'API' as TaskType,
          status: 'COMPLETED' as TaskStatus,
          httpMethod: 'GET',
          headers: [{ key: 'Authorization', value: 'Bearer token123' }],
          jsonPath: '$.current.temperature',
          tags: ['气象', 'API'],
          scheduleType: 'cron',
          cronExpression: '0 0 * * * ?',
          createTime: new Date().getTime() - 259200000,
          lastExecuteTime: new Date().getTime() - 10800000
        }
      ];
      loading.value = false;
    }, 500);
  } catch (error) {
    console.error('加载任务列表失败', error);
    ElMessage.error('加载任务列表失败');
    loading.value = false;
  }
};

// 加载任务执行历史
const loadTaskHistory = async (taskId: string) => {
  loadingHistory.value = true;
  try {
    // TODO: 实际项目中应该调用API接口获取任务执行历史
    // const response = await api.getTaskHistory(taskId);
    // taskHistory.value = response.data;
    
    // 模拟数据
    setTimeout(() => {
      taskHistory.value = [
        {
          id: '101',
          taskId: taskId,
          executeTime: new Date().getTime() - 3600000,
          status: 'SUCCESS' as ExecutionStatus,
          duration: 1250,
          resultData: { value: 45.6, unit: '%' },
          rawOutput: '.1.3.6.1.2.1.25.3.3.1.2 = 45.6',
          node: 'collector-node-01'
        },
        {
          id: '102',
          taskId: taskId,
          executeTime: new Date().getTime() - 7200000,
          status: 'SUCCESS' as ExecutionStatus,
          duration: 980,
          resultData: { value: 38.2, unit: '%' },
          rawOutput: '.1.3.6.1.2.1.25.3.3.1.2 = 38.2',
          node: 'collector-node-01'
        },
        {
          id: '103',
          taskId: taskId,
          executeTime: new Date().getTime() - 10800000,
          status: 'FAILURE' as ExecutionStatus,
          duration: 2100,
          errorMessage: 'Connection timed out',
          rawOutput: 'Error: Request timed out',
          node: 'collector-node-01'
        }
      ];
      loadingHistory.value = false;
    }, 300);
  } catch (error) {
    console.error('加载任务执行历史失败', error);
    ElMessage.error('加载任务执行历史失败');
    loadingHistory.value = false;
  }
};

// 添加任务
const addTask = () => {
  currentTask.value = {} as Task;
  formTitle.value = '创建采集任务';
  showTaskForm.value = true;
  viewMode.value = false;
};

// 编辑任务
const editTask = (task: Task) => {
  currentTask.value = { ...task };
  formTitle.value = '编辑采集任务';
  showTaskForm.value = true;
  viewMode.value = false;
};

// 查看任务详情
const viewTask = async (task: Task) => {
  currentTask.value = { ...task };
  formTitle.value = '任务详情';
  showTaskForm.value = true;
  viewMode.value = true;
  
  // 加载任务执行历史
  await loadTaskHistory(task.id);
};

// 从查看模式切换到编辑模式
const editCurrentTask = () => {
  formTitle.value = '编辑采集任务';
  viewMode.value = false;
};

// 执行任务
const executeTask = async (task: Task) => {
  try {
    // TODO: 实际项目中应该调用API接口执行任务
    // await api.executeTask(task.id);
    
    // 模拟执行
    ElMessage.success(`任务"${task.title}"已开始执行`);
    
    // 更新任务状态
    const index = tasks.value.findIndex(t => t.id === task.id);
    if (index !== -1) {
      tasks.value[index].status = 'RUNNING';
    }
  } catch (error) {
    console.error('执行任务失败', error);
    ElMessage.error('执行任务失败');
  }
};

// 取消任务
const cancelTask = async (task: Task) => {
  try {
    // TODO: 实际项目中应该调用API接口取消任务
    // await api.cancelTask(task.id);
    
    // 模拟取消
    ElMessage.success(`任务"${task.title}"已取消`);
    
    // 更新任务状态
    const index = tasks.value.findIndex(t => t.id === task.id);
    if (index !== -1) {
      tasks.value[index].status = 'CANCELED';
    }
  } catch (error) {
    console.error('取消任务失败', error);
    ElMessage.error('取消任务失败');
  }
};

// 删除任务
const deleteTask = async (task: Task) => {
  try {
    await ElMessageBox.confirm(
      `确认要删除任务"${task.title}"吗？此操作不可恢复。`,
      '删除任务',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    // TODO: 实际项目中应该调用API接口删除任务
    // await api.deleteTask(task.id);
    
    // 模拟删除
    ElMessage.success(`任务"${task.title}"已删除`);
    
    // 从列表中移除
    tasks.value = tasks.value.filter(t => t.id !== task.id);
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除任务失败', error);
      ElMessage.error('删除任务失败');
    }
  }
};

// 提交任务表单
const submitTask = async (formData: Task) => {
  submitting.value = true;
  try {
    if (formData.id) {
      // 编辑现有任务
      // TODO: 实际项目中应该调用API接口更新任务
      // await api.updateTask(formData.id, formData);
      
      // 模拟更新
      const index = tasks.value.findIndex(t => t.id === formData.id);
      if (index !== -1) {
        tasks.value[index] = { ...formData };
      }
      ElMessage.success('任务更新成功');
    } else {
      // 创建新任务
      // TODO: 实际项目中应该调用API接口创建任务
      // const response = await api.createTask(formData);
      // const newTask = response.data;
      
      // 模拟创建
      const newTask = {
        ...formData,
        id: Date.now().toString(),
        status: 'CREATED' as TaskStatus,
        createTime: Date.now(),
        lastExecuteTime: null
      };
      tasks.value.unshift(newTask);
      ElMessage.success('任务创建成功');
    }
    
    // 关闭表单，返回列表
    showTaskForm.value = false;
  } catch (error) {
    console.error('提交任务失败', error);
    ElMessage.error('提交任务失败');
  } finally {
    submitting.value = false;
  }
};

// 取消编辑
const cancelEdit = () => {
  showTaskForm.value = false;
};

// 查看执行详情
const viewExecutionDetail = (execution: TaskExecution) => {
  currentExecution.value = execution;
  executionDetailVisible.value = true;
  executionDetailTab.value = 'data';
};

// 处理任务选择变化
const handleSelectionChange = (selection: Task[]) => {
  selectedTasks.value = selection;
};

// 批量执行任务
const batchExecute = () => {
  batchActionType.value = 'execute';
  batchConfirmVisible.value = true;
};

// 批量删除任务
const batchDelete = () => {
  batchActionType.value = 'delete';
  batchConfirmVisible.value = true;
};

// 确认批量操作
const confirmBatchAction = async () => {
  batchActionLoading.value = true;
  try {
    const taskIds = selectedTasks.value.map(task => task.id);
    
    if (batchActionType.value === 'execute') {
      // TODO: 实际项目中应该调用API接口批量执行任务
      // await api.batchExecuteTasks(taskIds);
      
      // 模拟执行
      ElMessage.success(`已开始执行${taskIds.length}个任务`);
      
      // 更新任务状态
      tasks.value.forEach(task => {
        if (taskIds.includes(task.id)) {
          task.status = 'RUNNING';
        }
      });
    } else if (batchActionType.value === 'delete') {
      // TODO: 实际项目中应该调用API接口批量删除任务
      // await api.batchDeleteTasks(taskIds);
      
      // 模拟删除
      ElMessage.success(`已删除${taskIds.length}个任务`);
      
      // 从列表中移除
      tasks.value = tasks.value.filter(task => !taskIds.includes(task.id));
    }
    
    batchConfirmVisible.value = false;
    // 清空选择
    selectedTasks.value = [];
  } catch (error) {
    console.error('批量操作失败', error);
    ElMessage.error('批量操作失败');
  } finally {
    batchActionLoading.value = false;
  }
};

// 格式化日期时间
const formatDateTime = (timestamp: number | null): string => {
  if (!timestamp) return '未执行';
  const date = new Date(timestamp);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

// 获取任务状态类型和文本
const getStatusType = (status: TaskStatus): string => {
  const statusMap: Record<TaskStatus, string> = {
    'CREATED': 'info',
    'RUNNING': 'primary',
    'PAUSED': 'warning',
    'COMPLETED': 'success',
    'CANCELED': 'danger',
    'FAILED': 'danger'
  };
  return statusMap[status] || 'info';
};

const getStatusText = (status: TaskStatus): string => {
  const statusMap: Record<TaskStatus, string> = {
    'CREATED': '已创建',
    'RUNNING': '运行中',
    'PAUSED': '已暂停',
    'COMPLETED': '已完成',
    'CANCELED': '已取消',
    'FAILED': '失败'
  };
  return statusMap[status] || '未知';
};

// 获取执行状态类型和文本
const getExecutionStatusType = (status: ExecutionStatus): string => {
  const statusMap: Record<ExecutionStatus, string> = {
    'SUCCESS': 'success',
    'FAILURE': 'danger',
    'RUNNING': 'primary',
    'CANCELED': 'warning'
  };
  return statusMap[status] || 'info';
};

const getExecutionStatusText = (status: ExecutionStatus): string => {
  const statusMap: Record<ExecutionStatus, string> = {
    'SUCCESS': '成功',
    'FAILURE': '失败',
    'RUNNING': '执行中',
    'CANCELED': '已取消'
  };
  return statusMap[status] || '未知';
};

// 获取间隔单位文本
const getIntervalUnitText = (unit: IntervalUnit): string => {
  const unitMap: Record<IntervalUnit, string> = {
    'SECONDS': '秒',
    'MINUTES': '分钟',
    'HOURS': '小时',
    'DAYS': '天'
  };
  return unitMap[unit] || '';
};

// 格式化结果数据
const formatResultData = (data: any): string => {
  try {
    if (typeof data === 'string') {
      return data;
    }
    return JSON.stringify(data, null, 2);
  } catch (e) {
    return String(data);
  }
};

// 初始化
onMounted(() => {
  loadTasks();
});
</script>

<style lang="scss" scoped>
.task-management {
  width: 100%;
  height: 100%;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 0;
    
    .title-section {
      h2 {
        margin: 0;
        margin-bottom: 8px;
      }
      
      .subtitle {
        color: #909399;
      }
    }
    
    .action-section {
      display: flex;
      gap: 8px;
    }
  }
  
  .form-card {
    margin-top: 16px;
    
    .form-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h3 {
        margin: 0;
      }
    }
  }
  
  .task-view {
    .history-section {
      margin-top: 24px;
      
      h4 {
        margin-bottom: 16px;
      }
    }
  }
  
  .execution-tabs {
    margin-top: 16px;
    
    .result-data,
    .error-message,
    .raw-output {
      background-color: #f5f7fa;
      padding: 16px;
      border-radius: 4px;
      white-space: pre-wrap;
      font-family: monospace;
      max-height: 400px;
      overflow-y: auto;
    }
    
    .error-message {
      color: #f56c6c;
    }
  }
}
</style> 