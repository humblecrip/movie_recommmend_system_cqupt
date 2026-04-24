const base = {
    get() {
        return {
            url : "http://localhost:8080/springbootdo4wek3z/",
            name: "springbootdo4wek3z",
            // 退出到首页链接
            indexUrl: 'http://localhost:8080/springbootdo4wek3z/front/dist/index.html'
        };
    },
    getProjectName(){
        return {
            projectName: "基于协同过滤的电影推荐系统设计与实现"
        } 
    }
}
export default base
