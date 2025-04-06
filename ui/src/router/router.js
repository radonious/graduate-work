import { createRouter, createWebHistory } from "vue-router";
import { authService } from "../service/authService";
import Login from "../views/Login.vue";
import Registration from "../views/Registration.vue";
import Home from "../views/Home.vue"
import History from "../views/History.vue"
import About from "../views/About.vue"
import Profile from "../views/Profile.vue"


const routes = [
    { path: "/login", component: Login },
    { path: "/register", component: Registration },
    { path: "/home", component: Home },
    { path: "/history", component: History },
    { path: "/about", component: About },
    { path: "/profile", component: Profile },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

const publicRoutes = ["/login", "/register"];
const protectedRoutes = [
    "/",
    "/home",
    "/profile",
    "/about",
    "/history",
];

router.beforeEach((to, from, next) => {
    if (publicRoutes.includes(to.path) && authService.hasRefreshToken()) {
        next("/home");
    } else if (protectedRoutes.includes(to.path) && !authService.hasRefreshToken()) {
        next("/login");
    } else {
        next();
    }
});

export default router;