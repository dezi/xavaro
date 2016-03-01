



var medis = WebAppRequest.loadSync("medi.csv");

var pre = document.createElement("pre");
document.body.appendChild(pre);
pre.innerHTML = medis;
