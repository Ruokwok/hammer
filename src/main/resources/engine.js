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
let _SESSION = {};
function sessionStart() {
    var data = System.getSession();
    for (var key in data) {
        _SESSION[key] = data[key];
    }
}
function sessionClose() {
    System.sessionClose();
}