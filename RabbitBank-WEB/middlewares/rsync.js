const { exec } = require('child_process');

function runRsync() {
    var val1 = "~/keypair_shield.pem"; // 개인키 파일 경로
    var val2 = "~/AWS-ShieldBank/file/*"; // 동기화할 폴더 경로
    var val3 = "ubuntu@10.0.22.223:~/AWS-ShieldBank/file"; // 동기화할 다른 파일 경로

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
//     // key파일 권한 뭐시기 오류뜨면 
//     // -o StrictHostKeyChecking=no 해당 옵션 추가
//   const rsyncCommand = `rsync -avz -e "ssh -o StrictHostKeyChecking=no -i ~/keypair_shield.pem" ~/AWS-ShieldBank/file/ ubuntu@10.0.20.207:~/AWS-ShieldBank/file`;

//   exec(rsyncCommand, (error, stdout, stderr) => {
//       if (error) {
//           console.error(`Error: ${error.message}`);
//           return;
//       }
//       if (stderr) {
//           console.error(`stderr: ${stderr}`);
//           return;
//       }
//       console.log(`stdout: ${stdout}`);
//   });
// }
