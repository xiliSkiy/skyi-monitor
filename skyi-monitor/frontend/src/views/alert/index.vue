<template>
  <div class="app-container">
    <el-card class="filter-container" shadow="never">
      <div class="filter-item">
        <el-form :inline="true" :model="queryParams" class="demo-form-inline">
          <el-form-item label="告警级别">
            <el-select v-model="queryParams.severity" placeholder="选择级别" clearable>
              <el-option label="严重" value="CRITICAL" />
              <el-option label="重要" value="MAJOR" />
              <el-option label="次要" value="MINOR" />
              <el-option label="警告" value="WARNING" />
              <el-option label="信息" value="INFO" />
            </el-select>
          </el-form-item>
          <el-form-item label="告警状态">
            <el-select v-model="queryParams.status" placeholder="选择状态" clearable>
              <el-option label="活动" value="ACTIVE" />
              <el-option label="已确认" value="ACKNOWLEDGED" />
              <el-option label="已解决" value="RESOLVED" />
              <el-option label="已过期" value="EXPIRED" />
            </el-select>
          </el-form-item>
          <el-form-item label="资产">
            <el-input v-model="queryParams.asset" placeholder="资产名称" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-row :gutter="20" class="dashboard-container">
      <el-col :span="24" :lg="6">
        <el-card class="stats-card">
          <template #header>
            <div class="card-header">
              <span>告警统计</span>
              <el-button type="text" @click="refreshStats">刷新</el-button>
            </div>
          </template>
          <div class="alert-stats" v-loading="statsLoading">
            <div class="stat-item">
              <div class="stat-label">严重</div>
              <div class="stat-value critical">{{ alertStats.critical || 0 }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">重要</div>
              <div class="stat-value major">{{ alertStats.major || 0 }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">次要</div>
              <div class="stat-value minor">{{ alertStats.minor || 0 }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">警告</div>
              <div class="stat-value warning">{{ alertStats.warning || 0 }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">信息</div>
              <div class="stat-value info">{{ alertStats.info || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="24" :lg="18">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>告警列表</span>
              <div>
                <el-button type="primary" size="small" @click="handleRefresh">刷新</el-button>
              </div>
            </div>
          </template>
          <alert-alert-list
            :alerts="alerts"
            :loading="loading"
            :current-page="queryParams.page"
            :page-size="queryParams.size"
            :total="total"
            :show-pagination="true"
            @view="handleViewDetail"
            @acknowledge="handleAcknowledge"
            @resolve="handleResolve"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </el-card>
      </el-col>
    </el-row>

    <!-- 确认告警对话框 -->
    <el-dialog
      title="确认告警"
      v-model="acknowledgeDialogVisible"
      width="30%"
    >
      <el-form :model="acknowledgeForm" label-width="80px">
        <el-form-item label="备注" prop="comment">
          <el-input
            v-model="acknowledgeForm.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入确认备注（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="acknowledgeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAcknowledge" :loading="acknowledgeLoading">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 解决告警对话框 -->
    <el-dialog
      title="解决告警"
      v-model="resolveDialogVisible"
      width="30%"
    >
      <el-form :model="resolveForm" label-width="80px" :rules="resolveFormRules" ref="resolveFormRef">
        <el-form-item label="备注" prop="comment">
          <el-input
            v-model="resolveForm.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入解决备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resolveDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmResolve" :loading="resolveLoading">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  getAlerts, 
  getAlertStatsBySeverity,
  acknowledgeAlert,
  resolveAlert
} from '@/api/alert';

const router = useRouter();

// 告警列表数据
const alerts = ref<any[]>([]);
const loading = ref(false);
const total = ref(0);

// 告警统计数据
const alertStats = reactive({
  critical: 0,
  major: 0,
  minor: 0,
  warning: 0,
  info: 0
});
const statsLoading = ref(false);

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  severity: '',
  status: '',
  asset: '',
  startTime: '',
  endTime: ''
});

// 确认告警相关
const acknowledgeDialogVisible = ref(false);
const acknowledgeLoading = ref(false);
const acknowledgeForm = reactive({
  id: '',
  comment: ''
});
const currentAlert = ref<any>(null);

// 解决告警相关
const resolveDialogVisible = ref(false);
const resolveLoading = ref(false);
const resolveForm = reactive({
  id: '',
  comment: ''
});
const resolveFormRules = {
  comment: [
    { required: true, message: '请输入解决备注', trigger: 'blur' },
    { min: 2, max: 200, message: '长度在 2 到 200 个字符', trigger: 'blur' }
  ]
};
const resolveFormRef = ref();

// 获取告警列表
const fetchAlerts = async () => {
  loading.value = true;
  try {
    const params = {
      page: queryParams.page - 1, // 后端页码从0开始
      size: queryParams.size,
      severity: queryParams.severity || undefined,
      status: queryParams.status || undefined,
      asset: queryParams.asset || undefined,
      startTime: queryParams.startTime || undefined,
      endTime: queryParams.endTime || undefined
    };
    
    const response = await getAlerts(params);
    if (response.code === 200) {
      alerts.value = response.data.content || [];
      total.value = response.data.totalElements || 0;
    } else {
      ElMessage.error(response.message || '获取告警列表失败');
    }
  } catch (error) {
    console.error('获取告警列表出错', error);
    ElMessage.error('获取告警列表出错');
  } finally {
    loading.value = false;
  }
};

// 获取告警统计
const fetchAlertStats = async () => {
  statsLoading.value = true;
  try {
    const response = await getAlertStatsBySeverity();
    if (response.code === 200) {
      // 转换后端返回的数据格式
      const data = response.data || {};
      alertStats.critical = data.CRITICAL || 0;
      alertStats.major = data.MAJOR || 0;
      alertStats.minor = data.MINOR || 0;
      alertStats.warning = data.WARNING || 0;
      alertStats.info = data.INFO || 0;
    } else {
      ElMessage.error(response.message || '获取告警统计失败');
    }
  } catch (error) {
    console.error('获取告警统计出错', error);
  } finally {
    statsLoading.value = false;
  }
};

// 处理查询
const handleQuery = () => {
  queryParams.page = 1;
  fetchAlerts();
};

// 重置查询
const resetQuery = () => {
  queryParams.severity = '';
  queryParams.status = '';
  queryParams.asset = '';
  queryParams.startTime = '';
  queryParams.endTime = '';
  handleQuery();
};

// 刷新告警列表
const handleRefresh = () => {
  fetchAlerts();
};

// 刷新统计数据
const refreshStats = () => {
  fetchAlertStats();
};

// 处理页码变化
const handleCurrentChange = (page: number) => {
  queryParams.page = page;
  fetchAlerts();
};

// 处理每页数量变化
const handleSizeChange = (size: number) => {
  queryParams.size = size;
  queryParams.page = 1;
  fetchAlerts();
};

// 查看告警详情
const handleViewDetail = (alert: any) => {
  router.push({
    name: 'AlertDetail',
    params: { id: alert.id }
  });
};

// 处理确认告警
const handleAcknowledge = (alert: any) => {
  currentAlert.value = alert;
  acknowledgeForm.id = alert.id;
  acknowledgeForm.comment = '';
  acknowledgeDialogVisible.value = true;
};

// 确认操作
const confirmAcknowledge = async () => {
  acknowledgeLoading.value = true;
  try {
    const response = await acknowledgeAlert(acknowledgeForm.id, {
      acknowledgedBy: 'admin', // 当前系统可能没有用户系统，暂时写死
      comment: acknowledgeForm.comment
    });
    
    if (response.code === 200) {
      ElMessage.success('告警确认成功');
      acknowledgeDialogVisible.value = false;
      fetchAlerts();
      fetchAlertStats();
    } else {
      ElMessage.error(response.message || '告警确认失败');
    }
  } catch (error) {
    console.error('告警确认出错', error);
    ElMessage.error('告警确认操作失败');
  } finally {
    acknowledgeLoading.value = false;
  }
};

// 处理解决告警
const handleResolve = (alert: any) => {
  currentAlert.value = alert;
  resolveForm.id = alert.id;
  resolveForm.comment = '';
  resolveDialogVisible.value = true;
};

// 解决操作
const confirmResolve = async () => {
  resolveFormRef.value?.validate(async (valid: boolean) => {
    if (!valid) return;
    
    resolveLoading.value = true;
    try {
      const response = await resolveAlert(resolveForm.id, {
        resolvedBy: 'admin', // 当前系统可能没有用户系统，暂时写死
        comment: resolveForm.comment
      });
      
      if (response.code === 200) {
        ElMessage.success('告警解决成功');
        resolveDialogVisible.value = false;
        fetchAlerts();
        fetchAlertStats();
      } else {
        ElMessage.error(response.message || '告警解决失败');
      }
    } catch (error) {
      console.error('告警解决出错', error);
      ElMessage.error('告警解决操作失败');
    } finally {
      resolveLoading.value = false;
    }
  });
};

// 组件挂载时加载数据
onMounted(() => {
  fetchAlerts();
  fetchAlertStats();
});
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}

.filter-container {
  margin-bottom: 20px;
}

.dashboard-container {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.alert-stats {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-label {
  font-weight: bold;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
  
  &.critical {
    color: #F56C6C;
  }
  
  &.major {
    color: #E6A23C;
  }
  
  &.minor {
    color: #E6A23C;
  }
  
  &.warning {
    color: #F2C037;
  }
  
  &.info {
    color: #909399;
  }
}
</style> 