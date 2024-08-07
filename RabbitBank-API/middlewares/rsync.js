const { exec } = require('child_process');

function runRsync() {
    var val1 = "~/keypair_shield.pem";
    var val2 = "~/AWS-ShieldBank/file/*";
    var val3 = "ubuntu@10.0.22.223:~/AWS-ShieldBank/file";

    const rsyncCommand = `rsync -avz -e "ssh -i${val1}" ${val2} ${val3}`;

    exec(rsyncCommand, (error, stdout, stderr) => {
        if (error) {
            console.error(`Error: ${error.message}`);
            return;
        }
        if (stderr) {
            console.error(`stderr: ${stderr}`);
            return;
        }
        console.log(`stdout: ${stdout}`);
    });
    next();
}
module.exports = runRsync;


// function runRsync() {
//     const rsyncCommand = 'rsync -avz -e "ssh -i ~/keypair_shield.pem" ~/AWS-ShieldBank/file/* ubuntu@10.0.20.207:~/AWS-ShieldBank/file';
  
//     exec(rsyncCommand, (error, stdout, stderr) => {
//         if (error) {
//             console.error(`Error: ${error.message}`);
//             return;
//         }
//         if (stderr) {
//             console.error(`stderr: ${stderr}`);
//             return;
//         }
//         console.log(`stdout: ${stdout}`);
//     });
//   }