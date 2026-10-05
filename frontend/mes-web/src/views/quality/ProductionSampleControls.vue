<script setup lang="ts">
import {ref} from 'vue'
import {useAuthStore} from '../../stores/auth'
import {type IncomingRow} from './incomingModel'
import {permits} from './productionQualityModel'
import QualityCommandForm from './QualityCommandForm.vue'
const props=defineProps<{record:IncomingRow;create?:boolean}>(),emit=defineEmits<{saved:[IncomingRow];cancel:[]}>(),auth=useAuthStore(),opened=ref(false)
</script>
<template><a-button v-if="!create&&permits(record,'receive')&&auth.can('qms:sample:receive')" @click="opened=true">接收生产样品</a-button><a-card v-if="create||opened" :title="create?'新增生产样品':'接收生产样品'" class="form-section"><QualityCommandForm :schema-name="create?'ProductionSampleCreateCommand':'ProductionSampleReceiveCommand'" :url="create?'/samples':'/samples/'+record.id+'/receive'" :source="record" :initial-value="create?{investigationScope:'PRODUCTION'}:{}" @saved="r=>{opened=false;emit('saved',r)}" @cancel="opened=false;emit('cancel')"/></a-card></template>
