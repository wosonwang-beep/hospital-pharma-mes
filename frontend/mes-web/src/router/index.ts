import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '../layouts/AppLayout.vue'
import DashboardView from '../views/dashboard/DashboardView.vue'
export default createRouter({history:createWebHistory(),routes:[{path:'/',component:AppLayout,children:[{path:'',name:'dashboard',component:DashboardView}]}]})
