# Important Session Layer Tags

## 7. BegSeqNo

## 16. EndSeqNo

## 8. BeginString (FIX4/4.2 // FIXT // LFIXT)

## 34. MsgSeqNum

## 35. MessageTypeGrp

| **value** | **meaning**     |
|-----------|-----------------|
| 0         | HeartBeat       |
| 1         | TestRequest     |
| 2         | ResendRequest   |
| 3         | Rejected        |
| 4         | SequenceReset   |
| 5         | logout          |
| 8         | ExecutionReport |
| A         | logon           |
| D         | NewOrderSingle  |

## 36. NewSeqNum

## 43. PossDupFlag (Y/N)

Y = re-transmission when 35=A (logon)

## 49. SenderCompID

## 50. SenderSubID

## 52. SendingTime

must be set to the time the message is **transmitted by the FIX session processor**, not the time the message was queued
for transmission.

## 56. TargetCompID

## 57. TargetSubID

## 58. Text

used for some case when need comment, like in 35=5 (logout), the field is used for indicating the error

## 108. HeartBtInt

## 123. GapFillFlag

## 141. ResetSeqNumFlag

## 142. SenderLocationID

## 143. TargetLocationID

# Important Application Layer Tags

## 1. Account

## 15. Currency

## 40. OrderType

## 97. PossResend

## 100. ExDestination

## 204. CustomerOrFirm

## 207. SecurityExchange


