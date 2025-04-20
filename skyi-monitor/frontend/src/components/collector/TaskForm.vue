<template>
  <div class="task-form">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      :disabled="loading"
    >
      <el-form-item label="任务名称" prop="title">
        <el-input v-model="form.title" placeholder="请输入任务名称" />
      </el-form-item>

      <el-form-item label="任务描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          placeholder="请输入任务描述"
          :rows="3"
        />
      </el-form-item>

      <el-form-item label="采集目标" prop="target">
        <el-input v-model="form.target" placeholder="请输入IP地址或主机名">
          <template #append v-if="form.type === 'SNMP' || form.type === 'SSH'">
            <el-input v-model="form.port" style="width: 80px;" placeholder="端口">
              <template #prepend>:</template>
            </el-input>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="采集类型" prop="type">
        <el-select v-model="form.type" placeholder="请选择采集类型" style="width: 100%">
          <el-option label="SNMP" value="SNMP" />
          <el-option label="API" value="API" />
          <el-option label="SSH" value="SSH" />
          <el-option label="脚本" value="SCRIPT" />
        </el-select>
      </el-form-item>

      <!-- SNMP配置 -->
      <template v-if="form.type === 'SNMP'">
        <el-form-item label="SNMP版本" prop="snmpVersion">
          <el-select v-model="form.snmpVersion" placeholder="请选择SNMP版本" style="width: 100%">
            <el-option label="v1" value="1" />
            <el-option label="v2c" value="2c" />
            <el-option label="v3" value="3" />
          </el-select>
        </el-form-item>

        <el-form-item label="Community" prop="community" v-if="form.snmpVersion === '1' || form.snmpVersion === '2c'">
          <el-input v-model="form.community" placeholder="请输入Community字符串" />
        </el-form-item>

        <template v-if="form.snmpVersion === '3'">
          <el-form-item label="安全级别" prop="securityLevel">
            <el-select v-model="form.securityLevel" placeholder="请选择安全级别" style="width: 100%">
              <el-option label="noAuthNoPriv" value="noAuthNoPriv" />
              <el-option label="authNoPriv" value="authNoPriv" />
              <el-option label="authPriv" value="authPriv" />
            </el-select>
          </el-form-item>

          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" />
          </el-form-item>

          <template v-if="form.securityLevel !== 'noAuthNoPriv'">
            <el-form-item label="认证协议" prop="authProtocol">
              <el-select v-model="form.authProtocol" placeholder="请选择认证协议" style="width: 100%">
                <el-option label="MD5" value="MD5" />
                <el-option label="SHA" value="SHA" />
              </el-select>
            </el-form-item>

            <el-form-item label="认证密码" prop="authPassword">
              <el-input v-model="form.authPassword" placeholder="请输入认证密码" show-password />
            </el-form-item>
          </template>

          <template v-if="form.securityLevel === 'authPriv'">
            <el-form-item label="加密协议" prop="privProtocol">
              <el-select v-model="form.privProtocol" placeholder="请选择加密协议" style="width: 100%">
                <el-option label="DES" value="DES" />
                <el-option label="AES" value="AES" />
              </el-select>
            </el-form-item>

            <el-form-item label="加密密码" prop="privPassword">
              <el-input v-model="form.privPassword" placeholder="请输入加密密码" show-password />
            </el-form-item>
          </template>
        </template>

        <el-form-item label="OID列表" prop="oids">
          <el-tag
            v-for="(oid, index) in form.oids"
            :key="index"
            closable
            @close="handleRemoveOid(index)"
            style="margin-right: 10px; margin-bottom: 10px;"
          >
            {{ oid }}
          </el-tag>
          <el-input
            v-if="inputOidVisible"
            ref="inputOidRef"
            v-model="inputOidValue"
            class="input-new-oid"
            size="small"
            @keyup.enter="handleAddOid"
            @blur="handleAddOid"
          />
          <el-button v-else class="button-new-oid" size="small" @click="showOidInput">
            + 添加OID
          </el-button>
        </el-form-item>
      </template>

      <!-- API配置 -->
      <template v-if="form.type === 'API'">
        <el-form-item label="请求方法" prop="httpMethod">
          <el-select v-model="form.httpMethod" placeholder="请选择HTTP方法" style="width: 100%">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>

        <el-form-item label="请求头" prop="headers">
          <div v-for="(item, index) in form.headers" :key="index" class="key-value-item">
            <el-input v-model="item.key" placeholder="Header名称" style="width: 40%; margin-right: 10px;" />
            <el-input v-model="item.value" placeholder="Header值" style="width: 40%; margin-right: 10px;" />
            <el-button type="danger" icon="el-icon-delete" circle @click="removeHeader(index)" />
          </div>
          <el-button type="primary" size="small" @click="addHeader">添加请求头</el-button>
        </el-form-item>

        <el-form-item label="请求体" prop="requestBody" v-if="form.httpMethod !== 'GET'">
          <el-input
            v-model="form.requestBody"
            type="textarea"
            placeholder="请输入请求体(JSON格式)"
            :rows="5"
          />
        </el-form-item>

        <el-form-item label="响应解析" prop="jsonPath">
          <el-input v-model="form.jsonPath" placeholder="请输入JsonPath表达式">
            <template #prepend>JsonPath</template>
          </el-input>
        </el-form-item>
      </template>

      <!-- SSH配置 -->
      <template v-if="form.type === 'SSH'">
        <el-form-item label="用户名" prop="sshUsername">
          <el-input v-model="form.sshUsername" placeholder="请输入SSH用户名" />
        </el-form-item>

        <el-form-item label="认证方式" prop="sshAuthType">
          <el-radio-group v-model="form.sshAuthType">
            <el-radio value="password">密码</el-radio>
            <el-radio value="key">密钥</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="密码" prop="sshPassword" v-if="form.sshAuthType === 'password'">
          <el-input v-model="form.sshPassword" placeholder="请输入SSH密码" show-password />
        </el-form-item>

        <el-form-item label="私钥" prop="sshPrivateKey" v-if="form.sshAuthType === 'key'">
          <el-input
            v-model="form.sshPrivateKey"
            type="textarea"
            placeholder="请输入SSH私钥"
            :rows="5"
          />
        </el-form-item>

        <el-form-item label="密钥密码" prop="sshKeyPassphrase" v-if="form.sshAuthType === 'key'">
          <el-input v-model="form.sshKeyPassphrase" placeholder="请输入密钥密码（如有）" show-password />
        </el-form-item>

        <el-form-item label="命令" prop="sshCommand">
          <el-input
            v-model="form.sshCommand"
            type="textarea"
            placeholder="请输入要执行的命令"
            :rows="3"
          />
        </el-form-item>

        <el-form-item label="输出解析" prop="sshOutputParser">
          <el-select v-model="form.sshOutputParser" placeholder="请选择输出解析方式" style="width: 100%">
            <el-option label="原始文本" value="raw" />
            <el-option label="正则表达式" value="regex" />
            <el-option label="JSON" value="json" />
          </el-select>
        </el-form-item>

        <el-form-item label="解析表达式" prop="sshParserExpression" v-if="form.sshOutputParser !== 'raw'">
          <el-input
            v-model="form.sshParserExpression"
            placeholder="请输入解析表达式"
            :rows="3"
          />
        </el-form-item>
      </template>

      <!-- 脚本配置 -->
      <template v-if="form.type === 'SCRIPT'">
        <el-form-item label="脚本类型" prop="scriptType">
          <el-select v-model="form.scriptType" placeholder="请选择脚本类型" style="width: 100%">
            <el-option label="Shell" value="shell" />
            <el-option label="Python" value="python" />
            <el-option label="JavaScript" value="javascript" />
          </el-select>
        </el-form-item>

        <el-form-item label="脚本内容" prop="scriptContent">
          <el-input
            v-model="form.scriptContent"
            type="textarea"
            placeholder="请输入脚本内容"
            :rows="10"
          />
        </el-form-item>

        <el-form-item label="参数" prop="scriptParams">
          <div v-for="(param, index) in form.scriptParams" :key="index" class="key-value-item">
            <el-input v-model="param.key" placeholder="参数名" style="width: 40%; margin-right: 10px;" />
            <el-input v-model="param.value" placeholder="参数值" style="width: 40%; margin-right: 10px;" />
            <el-button type="danger" icon="el-icon-delete" circle @click="removeScriptParam(index)" />
          </div>
          <el-button type="primary" size="small" @click="addScriptParam">添加参数</el-button>
        </el-form-item>
      </template>

      <el-form-item label="运行计划" prop="scheduleType">
        <el-radio-group v-model="form.scheduleType">
          <el-radio value="once">单次执行</el-radio>
          <el-radio value="interval">定时执行</el-radio>
          <el-radio value="cron">Cron表达式</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="执行时间" prop="executeTime" v-if="form.scheduleType === 'once'">
        <el-date-picker
          v-model="form.executeTime"
          type="datetime"
          placeholder="选择日期和时间"
          style="width: 100%"
        />
      </el-form-item>

      <template v-if="form.scheduleType === 'interval'">
        <el-form-item label="执行间隔" prop="interval">
          <el-input-number v-model="form.interval" :min="1" :max="99999" />
          <el-select v-model="form.intervalUnit" style="margin-left: 10px; width: 100px;">
            <el-option label="秒" value="SECONDS" />
            <el-option label="分钟" value="MINUTES" />
            <el-option label="小时" value="HOURS" />
            <el-option label="天" value="DAYS" />
          </el-select>
        </el-form-item>

        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始日期和时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束日期和时间（可选）"
            style="width: 100%"
          />
        </el-form-item>
      </template>

      <el-form-item label="Cron表达式" prop="cronExpression" v-if="form.scheduleType === 'cron'">
        <el-input v-model="form.cronExpression" placeholder="例如: 0 0 12 * * ?" />
      </el-form-item>

      <el-form-item label="标签" prop="tags">
        <el-tag
          v-for="tag in form.tags"
          :key="tag"
          closable
          @close="handleRemoveTag(tag)"
          style="margin-right: 10px; margin-bottom: 10px;"
        >
          {{ tag }}
        </el-tag>
        <el-input
          v-if="inputTagVisible"
          ref="inputTagRef"
          v-model="inputTagValue"
          class="input-new-tag"
          size="small"
          @keyup.enter="handleAddTag"
          @blur="handleAddTag"
        />
        <el-button v-else class="button-new-tag" size="small" @click="showTagInput">
          + 添加标签
        </el-button>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm" :loading="loading">提交</el-button>
        <el-button @click="resetForm">重置</el-button>
        <el-button @click="cancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, nextTick } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';

const props = defineProps({
  taskData: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['submit', 'cancel']);

const formRef = ref<FormInstance>();

// 表单数据结构
const form = reactive({
  id: '',
  title: '',
  description: '',
  target: '',
  port: '',
  type: 'SNMP',
  
  // SNMP相关配置
  snmpVersion: '2c',
  community: 'public',
  securityLevel: 'noAuthNoPriv',
  username: '',
  authProtocol: 'MD5',
  authPassword: '',
  privProtocol: 'DES',
  privPassword: '',
  oids: [] as string[],
  
  // API相关配置
  httpMethod: 'GET',
  headers: [] as { key: string; value: string }[],
  requestBody: '',
  jsonPath: '',
  
  // SSH相关配置
  sshUsername: '',
  sshAuthType: 'password',
  sshPassword: '',
  sshPrivateKey: '',
  sshKeyPassphrase: '',
  sshCommand: '',
  sshOutputParser: 'raw',
  sshParserExpression: '',
  
  // 脚本相关配置
  scriptType: 'shell',
  scriptContent: '',
  scriptParams: [] as { key: string; value: string }[],
  
  // 调度相关配置
  scheduleType: 'once',
  executeTime: null as Date | null,
  interval: 5,
  intervalUnit: 'MINUTES',
  startTime: null as Date | null,
  endTime: null as Date | null,
  cronExpression: '',
  
  // 其他配置
  tags: [] as string[]
});

// 表单验证规则
const rules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入任务名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2到50个字符之间', trigger: 'blur' }
  ],
  target: [
    { required: true, message: '请输入采集目标', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择采集类型', trigger: 'change' }
  ],
  snmpVersion: [
    { required: true, message: '请选择SNMP版本', trigger: 'change' }
  ],
  community: [
    { required: true, message: '请输入Community字符串', trigger: 'blur' }
  ],
  httpMethod: [
    { required: true, message: '请选择HTTP方法', trigger: 'change' }
  ],
  sshUsername: [
    { required: true, message: '请输入SSH用户名', trigger: 'blur' }
  ],
  sshPassword: [
    { required: true, message: '请输入SSH密码', trigger: 'blur' }
  ],
  sshCommand: [
    { required: true, message: '请输入要执行的命令', trigger: 'blur' }
  ],
  scriptContent: [
    { required: true, message: '请输入脚本内容', trigger: 'blur' }
  ],
  cronExpression: [
    { required: true, message: '请输入Cron表达式', trigger: 'blur' }
  ]
});

// OID标签相关
const inputOidRef = ref();
const inputOidVisible = ref(false);
const inputOidValue = ref('');

// 标签相关
const inputTagRef = ref();
const inputTagVisible = ref(false);
const inputTagValue = ref('');

// 初始化表单数据
onMounted(() => {
  if (props.taskData && Object.keys(props.taskData).length > 0) {
    Object.keys(props.taskData).forEach(key => {
      if (key in form) {
        // @ts-expect-error - dynamic property access
        form[key] = props.taskData[key];
      }
    });
  }
});

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate((valid, fields) => {
    if (valid) {
      // 根据不同调度类型处理日期
      const formData = { ...form };
      
      // 提交表单数据
      emit('submit', formData);
    } else {
      console.error('表单验证失败', fields);
    }
  });
};

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields();
  }
};

// 取消操作
const cancel = () => {
  emit('cancel');
};

// OID相关操作
const showOidInput = () => {
  inputOidVisible.value = true;
  nextTick(() => {
    inputOidRef.value?.focus();
  });
};

const handleAddOid = () => {
  if (inputOidValue.value) {
    if (!form.oids.includes(inputOidValue.value)) {
      form.oids.push(inputOidValue.value);
    }
  }
  inputOidVisible.value = false;
  inputOidValue.value = '';
};

const handleRemoveOid = (index: number) => {
  form.oids.splice(index, 1);
};

// 标签相关操作
const showTagInput = () => {
  inputTagVisible.value = true;
  nextTick(() => {
    inputTagRef.value?.focus();
  });
};

const handleAddTag = () => {
  if (inputTagValue.value) {
    if (!form.tags.includes(inputTagValue.value)) {
      form.tags.push(inputTagValue.value);
    }
  }
  inputTagVisible.value = false;
  inputTagValue.value = '';
};

const handleRemoveTag = (tag: string) => {
  const index = form.tags.indexOf(tag);
  if (index > -1) {
    form.tags.splice(index, 1);
  }
};

// API请求头相关操作
const addHeader = () => {
  form.headers.push({ key: '', value: '' });
};

const removeHeader = (index: number) => {
  form.headers.splice(index, 1);
};

// 脚本参数相关操作
const addScriptParam = () => {
  form.scriptParams.push({ key: '', value: '' });
};

const removeScriptParam = (index: number) => {
  form.scriptParams.splice(index, 1);
};
</script>

<style lang="scss" scoped>
.task-form {
  padding: 20px;
  
  .key-value-item {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
  }
  
  .input-new-tag,
  .input-new-oid {
    width: 200px;
    margin-right: 10px;
    vertical-align: bottom;
  }
}
</style> 