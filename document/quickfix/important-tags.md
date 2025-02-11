# Important Session Layer Tags

## 7. BegSeqNo

## 8. BeginString (FIX4/4.2 // FIXT // LFIXT)

## 16. EndSeqNo

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

## 11. ClOrdID
unique ID assigned by order routing firm. Uniqueness must be guaranteed within a single trading day.

Firms should consider embedding a date within the field to assure uniqueness across days when submitting 
multi-day orders.

## 15. Currency

## 21. HandlInst
1. Automated execution order, private, no Broker intervention
  - The order is processed entirely by electronic systems without any manual intervention by the broker.
  - The order remains private and is not exposed to other market participants.
2. Automated execution order, public, Broker intervention OK
  - The orde ris processed electronically, but the broker has discretion to intervene if necessary.
  - The order may be exposed to the public markets or other participants for better execution.
3. Manual order, best execution
  - The order is handled manually by the broker to achieve the best possible execution.
  - It allows the broker to use their judgment and discretion to execute the order. 

## 37. OrderID
Unique ID assigned by broker.  Uniquess be guaranteed within a single trading day.

## 40. OrderType

## 60. TransactTime
Time of execution/order creation(in UTC), sed in **Execution Reports**

## 97. PossResend

## 100. ExDestination

## 204. CustomerOrFirm
0. Customer
1. Firm

## 207. SecurityExchange


