const _COOKIE = System.getCookies();
const _SESSION = {};
function addCookie(name, value, path = Request.getPath(), domain = Request.getDomain(), age = -1, httpOnly = false) {
    System.putCookie(name, value, path, domain, age, httpOnly);
}
function removeCookie(name) {
    System.putCookie(name, null, null, null, 0, false);
}
function sessionStart(sec = 3600) {
    var data = System.getSession(sec);
    for (var key in data) {
        _SESSION[key] = data[key];
    }
}
function sessionClose() {
    System.sessionClose();
}
function getUploadFiles(name = null) {
    return System.getUploadParts(name);
}
function addHeader(key, value) {
    System.addHeader(key, value);
}