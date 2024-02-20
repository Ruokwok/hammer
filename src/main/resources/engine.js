const _COOKIE = System.getCookies();
function addCookie(name, value, path = Request.getPath(), domain = Request.getDomain(), age = -1, httpOnly = false) {
    System.putCookie(name, value, path, domain, age, httpOnly);
}

function removeCookie(name) {
    System.putCookie(name, null, null, null, 0, false);
}
function print(obj) {
    System.outputScript(obj);
}
function echo(obj) {
    print(obj);
}
function include(filename) {
    var file = getFile(filename);
    if (file.exists()) {
        var script = System.include(file.read(), file.getPath());
        eval(script);
    } else {
        throw new Error("the file \"" + filename + "\" is not exists.");
    }
}
function getFile(filename) {
    return Files.getFile(filename);
}
const _SESSION = {};
function sessionStart(sec = 3600) {
    var data = System.getSession(sec);
    for (var key in data) {
        _SESSION[key] = data[key];
    }
}
function sessionClose() {
    System.sessionClose();
}
function setStatus(code = 200) {
    System.setStatus(code);
}
function md5(str) {
    return Digest.md5(str);
}
function database(url, username, password) {
    return Database.connect(url, username, password);
}