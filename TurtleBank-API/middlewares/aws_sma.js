const { SNSClient, PublishCommand } = require("@aws-sdk/client-sns");

// AWS 설정
const REGION = "ap-northeast-2"; // 서울 리전
const snsClient = new SNSClient({ region: REGION });

// SMS 보내기 함수
async function sendSMS(phoneNumber, message) {
    // 한국 전화번호 형식 확인 및 변환
    const formattedPhoneNumber = formatKoreanPhoneNumber(phoneNumber);

    const params = {
        Message: message,
        PhoneNumber: formattedPhoneNumber,
        MessageAttributes: {
        'AWS.SNS.SMS.SenderID': {
            'DataType': 'String',
            'StringValue': 'MySenderID' // 발신자 ID (최대 11자)
        },
        'AWS.SNS.SMS.SMSType': {
            'DataType': 'String',
            'StringValue': 'Transactional' // 또는 'Promotional'
        }
        }
    };

    try {
        const data = await snsClient.send(new PublishCommand(params));
        console.log("메시지가 성공적으로 전송되었습니다. MessageID: " + data.MessageId);
        return data;
    } catch (err) {
        console.error("오류 발생:", err.stack);
        throw err;
    }
}

// 한국 전화번호 형식 변환 함수
function formatKoreanPhoneNumber(phoneNumber) {
    // 전화번호에서 하이픈 제거
    let cleaned = phoneNumber.replace(/-/g, '');
    // 국가 코드가 없으면 추가
    if (!cleaned.startsWith('+82')) {
        // 0으로 시작하면 0 제거
        cleaned = cleaned.startsWith('0') ? cleaned.slice(1) : cleaned;
        cleaned = '+82' + cleaned;
    }
    return cleaned;
}

    // 함수 사용 예
// sendSMS("010-1234-5678", "AWS SNS를 통한 테스트 메시지입니다.")
// .then(() => console.log("SMS가 성공적으로 전송되었습니다."))
// .catch((err) => console.error("SMS 전송 실패:", err));

module.exports = sendSMS;