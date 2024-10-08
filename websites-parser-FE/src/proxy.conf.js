
const API = process.env.API_URL || 'http://localhost:8080';

module.exports = {
  "/api/": {
    target: API,
    secure: false,
    changeOrigin: true,
    pathRewrite: {
      "^/api": ""
    }
  }
};