import { createRouter, createWebHistory } from "vue-router";
import { authService } from "../service/authService";
import Login from "../views/Login.vue";


const routes = [
    { path: "/login", component: Login },
    // { path: "/", component: Home },
    // { path: "/profile", component: Profile },
    // { path: "/about", component: About },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

const publicRoutes = ["/", "/login", "/register"];
const protectedRoutes = [
    "/home",
    "/profile",
    "/about",
    "/history",
];

router.beforeEach((to, from, next) => {
    if (publicRoutes.includes(to.path) && authService.hasRefreshToken()) {
        next("/form-list");
    } else if (protectedRoutes.includes(to.path) && !authService.hasRefreshToken()) {
        next("/login");
    } else {
        next();
    }
});

export default router;