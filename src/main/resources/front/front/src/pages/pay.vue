<template>
  <div class="container"  :style='{"margin":"0 200px 20px"}' style="padding: 150px 0;">
    <el-alert title="支付功能当前已停用" type="warning" :closable="false"></el-alert>
    <div class="disabled-notice">
      当前页面仅保留路由兼容性，暂不开放在线支付，也不会修改订单支付状态。
    </div>
    <!-- <div class="top-content">
      <span>收款人</span>
      <el-input style="width:300px" v-model="name" placeholder="收款人"></el-input>
      <span style="margin-left:20px">收款账号</span>
      <el-input style="width:300px" v-model="account" placeholder="收款账号"></el-input>
    </div> -->
    <!-- <div class="price-content">
      <span>金额</span>
      <span>￥99.0</span>
    </div> -->
    <div class="pay-type-content">
      <div class="pay-type-item">
        <el-radio v-model="type" label="微信支付" disabled></el-radio>
        <img src="@/assets/weixin.png" alt>
        <!-- <span>微信支付</span> -->
      </div>
      <div class="pay-type-item">
        <el-radio v-model="type" label="支付宝支付" disabled></el-radio>
        <img src="@/assets/zhifubao.png" alt>
        <!-- <span>支付宝支付</span> -->
      </div>
      <div class="pay-type-item">
        <el-radio v-model="type" label="建设银行" disabled></el-radio>
        <img src="@/assets/jianshe.png" alt>
        <!-- <span>建设银行</span> -->
      </div>
      <div class="pay-type-item">
        <el-radio v-model="type" label="农业银行" disabled></el-radio>
        <img src="@/assets/nongye.png" alt>
        <!-- <span>农业银行</span> -->
      </div>
      <div class="pay-type-item">
        <el-radio v-model="type" label="中国银行" disabled></el-radio>
        <img src="@/assets/zhongguo.png" alt>
        <!-- <span>中国银行</span> -->
      </div>
      <div class="pay-type-item">
        <el-radio v-model="type" label="交通银行" disabled></el-radio>
        <img src="@/assets/jiaotong.png" alt>
        <!-- <span>交通银行</span> -->
      </div>
    </div>
    <div class="buton-content">
      <el-button type="primary" disabled>支付已停用</el-button>
      <el-button @click="back()">返回</el-button>
    </div>
  </div>
</template>
<script>
// import { Message } from "element-ui";
export default {
  data() {
    return {
      type: "",
    };
  },
  methods: {
    submitTap() {
      this.$message.warning("支付功能当前未开放，订单支付状态不会在此页修改。");
    },
    back(){
      this.$router.go(-1);
    }
  }
};
</script>
<style lang="scss" scoped>
.container {
  margin: 10px;
  font-size: 14px;
  .disabled-notice {
    margin-top: 16px;
    color: #c45656;
    line-height: 1.8;
  }
  span {
    width: 60px;
  }
  .top-content {
    display: flex;
    align-items: center;
    padding: 20px;
  }
  .price-content {
    display: flex;
    align-items: center;
    margin-top: 20px;
    padding-bottom: 20px;
    padding: 20px;
    border-bottom: 1px solid #eeeeee;
    font-size: 20px;
    font-weight: bold;
    color: red;
  }
  .pay-type-content {
    display: flex;
    align-items: center;
    margin-top: 20px;
    flex-wrap: wrap;
    span {
      width: 100px;
    }
    .pay-type-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      width: 300px;
      margin: 20px;
      border: 1px solid #eeeeee;
      padding: 20px;
    }
  }
  .buton-content {
    margin: 20px;
  }
}
</style>
