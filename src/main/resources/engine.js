function print(obj) {
    System.outputScript(obj);
}
function echo(obj) {
    print(obj);
}
function include(filename) {
    var file = getFile(filename);
    if (file.exists()) {
        System.eval(file.read());
    } else {
        throw new Error("the file \"" + filename + "\" is not exists.");
    }
}
function getFile(filename) {
    return Files.getFile(filename);
}