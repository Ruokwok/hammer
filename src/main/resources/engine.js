function print(obj) {
    System.outputScript(obj);
}
function echo(obj = '') {
    print(obj);
}
function include(filename) {
    if (filename == undefined) return;
    var file = getFile(filename);
    if (file.exists()) {
        System.include(file.readString(), file.toString());
    } else {
        throw new Error("the file \"" + filename + "\" is not exists.");
    }
}
function getFile(filename) {
    return System.getFile(filename);
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
function curl(url) {
    return module("Http").get(url).body();
}
function sleep(time) {
    System.sleep(time);
}
function module(name) {
    return System.module(name);
}
function exit(code = -1) {
    System.stop(code);
}
function task(url, entry = null) {
    System.task(url, entry);
}