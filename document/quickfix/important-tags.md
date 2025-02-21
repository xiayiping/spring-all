# Important Session Layer Tags

## 1. Account

## 6. AvgPx

## 7. BegSeqNo

## 8. BeginString (FIX4/4.2 // FIXT // LFIXT)

## 9. Body Length

## 10. CheckSum

## 11. ClOrdID
unique ID assigned by order routing firm. Uniqueness must be guaranteed within a single trading day.

Firms should consider embedding a date within the field to assure uniqueness across days when submitting
multi-day orders.

## 15. Currency

## 16. EndSeqNo

## 17. ExecID

## 20. ExecTransType

0 = New

1 = Cancel

2 = Correct

3 = Status

## 21. HandlInst
1. Automated execution order, private, no Broker intervention
   - The order is processed entirely by electronic systems without any manual intervention by the broker.
   - The order remains private and is not exposed to other market participants.
2. Automated execution order, public, Broker intervention OK
   - The order is processed electronically, but the broker has discretion to intervene if necessary.
   - The order may be exposed to the public markets or other participants for better execution.
3. Manual order, best execution
   - The order is handled manually by the broker to achieve the best possible execution.
   - It allows the broker to use their judgment and discretion to execute the order.

## 22. IDSource

1 = CUSIP

2 = SEDOL

3 = QUIK

4 = ISIN number

5 = RIC code

6 = ISO Currency <15> Code

7 = ISO Country <421> Code

8 = Exchange Symbol

9 = Consolidated Tape Association (CTA) Symbol (SIAC CTS/CQS line format)

Stock orders can be routed either by ticker symbol using tag 55 (Symbol) or via security
identifiers. The latter method requires CUSIP or ISIN code specified in tag 48 (SecurityID). If
tag 48 is used, tag 22 must also be included which identifies the type of code used in tag 48.
Tag 55 could be omitted if stocks are identified via security identifiers. For preferred symbols,
the default configuration is to expect a space between symbol and suffix (example: ‘BRK B’).
However, we can also support tags 55 and 65, which would be 55=BRK / 65=B.

## 31. LastPx

## 32. LastShares

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

0 = Heartbeat <0>

1 = Test Request <1>

2 = Resend Request <2>

3 = Reject <3>

4 = Sequence Reset <4>

5 = Logout <5>

6 = Indication of Interest <6>

7 = Advertisement <7>

8 = Execution Report <8>

9 = Order Cancel Reject <9>

A = Logon 

B = News 

C = Email <C>

D = Order - Single <D>

E = Order - List <E>

F = Order Cancel Request <F>

G= Order Cancel/Replace Request <G>

H= Order Status Request <H>

J = Allocation <J>

K = List Cancel Request <K>

L = List Execute 

M = List Status Request <M>

N = List Status <N>

P = Allocation ACK <P>

Q = Don't Know Trade <Q> (DK)

R = Quote Request <R>

S = Quote 

T = Settlement Instructions <T>

V = Market Data Request <V>

W = Market Data-Snapshot/Full Refresh <W>

X = Market Data-Incremental Refresh <X>

Y = Market Data Request Reject <Y>

Z = Quote Cancel <Z>

a = Quote Status Request 

b = Quote Acknowledgement 

c = Security Definition Request <c>

d = Security Definition <d>

e = Security Status Request <e>

f = Security Status <f>

g = Trading Session Status Request <g>

h = Trading Session Status <h>

i = Mass Quote <i>

j = Business Message Reject <j>

k = Bid Request <k>

l = Bid Response <l> (lowercase L)

m = List Strike Price <m>

## 36. NewSeqNum

## 37. OrderID

Unique ID assigned by broker.  Uniques be guaranteed within a single trading day.

## 38. OrderQty

## 39. OrdStatus

0 = New

1 = Partially filled

2 = Filled

3 = Done for day

4 = Canceled

5 = Replaced

6 = Pending Cancel (e.g. result of Order Cancel Request <F>)

7 = Stopped

8 = Rejected

9 = Suspended

A = Pending New

B = Calculated

C = Expired

D = Accepted for bidding

E = Pending Replace (e.g. result of Order Cancel/Replace Request <G>)

## 40. OrderType

1 = Market

2 = Limit

3 = Stop

4 = Stop limit

5 = Market on close

6 = With or without

7 = Limit or better

8 = Limit with or without

9 = On basis

A = On close

B = Limit on close

C = Forex - Market

D = Previously quoted

E = Previously indicated

F = Forex - Limit

G = Forex - Swap

H = Forex - Previously Quoted

I = Funari (Limit Day Order with unexecuted portion handled as Market On Close. e.g. Japan)

P = Pegged

## 41. OrigClOrdId

## 43. PossDupFlag (Y/N)

Y = re-transmission when 35=A (logon)

## 44. Price

## 48. SecurityID

## 49. SenderCompID

## 50. SenderSubID

## 52. SendingTime

must be set to the time the message is **transmitted by the FIX session processor**, not the time the message was queued
for transmission.

## 54. Side

1 = Buy

2 = Sell

3 = Buy minus

4 = Sell plus

5 = Sell short

6 = Sell short exempt

7 = Undisclosed (valid for IOI and List Order messages only)

8 = Cross (orders where counterparty is an exchange, valid for all messages except IOIs)

9 = Cross short

## 56. TargetCompID

## 57. TargetSubID

## 58. Text

used for some case when need comment, like in 35=5 (logout), the field is used for indicating the error

## 60. TransactTime
Time of execution/order creation(in UTC), sed in **Execution Reports**

## 97. PossResend

## 98. EncryptMethod

- 0 None
- 1 PKCS
- .....

## 100. ExDestination

## 102. CxlRejReason

0 = Too late to cancel

1 = Unknown order

2 = Broker Option

3 = Order already in Pending Cancel or Pending Replace status

## 103. OrderRejReason

0 = Broker option

1 = Unknown symbol

2 = Exchange closed

3 = Order exceeds limit

4 = Too late to enter

5 = Unknown Order

6 = Duplicate Order (e.g. dupe ClOrdID <11>)

7 = Duplicate of a verbally communicated order

8 = Stale Order

## 108. HeartBtInt

## 123. GapFillFlag

## 141. ResetSeqNumFlag

## 142. SenderLocationID

## 143. TargetLocationID

## 150. ExecType

Valid values:

0 = New

1 = Partial fill

2 = Fill

3 = Done for day

4 = Canceled

5 = Replaced

6 = Pending Cancel (e.g. result of Order Cancel Request <F>)

7 = Stopped

8 = Rejected

9 = Suspended

A = Pending New

B = Calculated

C = Expired

D = Restated (ExecutionRpt sent unsolicited by sellside, with ExecRestatementReason <378> set)

E = Pending Replace (e.g. result of Order Cancel/Replace Request <G>)

## 204. CustomerOrFirm

- 0. Customer
- 1. Firm

## 207. SecurityExchange

https://www.onixs.biz/fix-dictionary/4.2/app_c.html

## 371. RefTagID

The tag number of the FIX field being referenced.

## 372. RefMsgType

The MsgType <35> of the FIX message being referenced.

## 373. SessionRejectReason

0 = Invalid tag number

1 = Required tag missing

2 = Tag not defined for this message type

3 = Undefined Tag

4 = Tag specified without a value

5 = Value is incorrect (out of range) for this tag

6 = Incorrect data format for value

7 = Decryption problem

8 = Signature <89> problem

9 = CompID problem

10 = SendingTime <52> accuracy problem

11 = Invalid MsgType <35>

# Comments

## Tag 21

Great question! Let's clarify exactly what "private" means in the context of **`HandlInst=1`** and how such orders work in practice.

### **Who are "Other Market Participants"?**
"Other market participants" refers to anyone participating in the market or trading venue, such as:

- Traders or firms submitting bids or offers.
- Market makers providing liquidity.
- Other brokers or institutions accessing the same market.
- Algorithmic or electronic trading systems operating in the market.

### **Does "private" mean my bid or ask is invisible to others?**
Yes, **`HandlInst=1`** means your order is not directly exposed to the public order book or market. Essentially, your order is processed **inside the broker's system** (or an electronic trading system) and does not appear on public trading venues like an exchange or ECN (Electronic Communication Network).

For example:
- Your **bid** or **ask** is not visible in the central order book where other traders would normally see it.
- Other participants cannot directly interact with your order because it is not "lit" (visible) in the public market.

### **How can the order be filled if others can't see it?**
Even if your order is private and not visible to others, there are mechanisms that still allow it to be filled:

1. **Internal Broker Matching:**
    - Many brokers operate internal crossing or matching engines where they match orders from their own clients. If another client of the same broker submits a compatible buy or sell order, your order can be matched internally without ever being sent to the public market.
    - This is often referred to as a "dark pool" or "internal liquidity pool."

2. **Smart Order Routing:**
    - The broker (or trading system) may route your order to a specific venue or liquidity pool to find a matching counterparty. However, the order itself remains hidden; it doesn't appear in the public order book.

3. **Execution Against Broker Inventory:**
    - Sometimes, the broker itself acts as the counterparty. For instance, if the broker has inventory (e.g., stocks they hold), they might fill your order directly from their own holdings without exposing it to the market.

4. **Sweep or Ping for Liquidity:**
    - The broker's systems may "ping" other liquidity pools or trading venues to look for matching orders without publicly displaying your order.

### **Why Use Private (Hidden) Orders?**
There are several reasons why traders might prefer private orders that are not exposed to the public market:

1. **Avoid Market Impact:**
    - Large orders, if made public, can move the market against you. For example, if you place a large buy order, the price may rise before your order is completely filled. Keeping the order private helps avoid this.

2. **Minimize Information Leakage:**
    - Exposing your bid or ask might give other participants clues about your trading strategy or intentions. Hidden orders help maintain anonymity and prevent others from taking advantage of your trades.

3. **Access to Better Liquidity:**
    - Brokers often have access to private pools of liquidity (e.g., dark pools) where large blocks of shares can be traded without affecting public markets.

4. **Faster Execution:**
    - Internal matching within a broker's system can often be faster than routing to public exchanges.

### **Limitations of Private Orders:**
While hidden orders provide benefits, there are also trade-offs:

- **Liquidity Access:** If your order is hidden, it may take longer to find a counterparty to fill the order because it is not visible to the entire market.
- **Price Discovery:** Public order books rely on visible bids and asks to determine market prices. Hidden orders do not contribute to this process.
- **Execution Risk:** If no matching counterparty is found internally or in private pools, the order might go unfilled.

### **In Summary:**
When you use **`HandlInst=1`**, your order is not exposed to "other market participants" in the public market. Instead, it is handled within the broker's systems or routed discreetly to private liquidity pools. While this can help with avoiding market impact and preserving anonymity, it may also limit liquidity and increase execution time if no matches are found internally or in private venues.

If you're concerned about how your orders are being handled, you can always discuss with your broker to make sure their execution strategy aligns with your goals.

No, private orders and public orders are **not completely separated**, and they *can* be filled or paired under certain conditions. However, the mechanics of how they interact depend on how the broker or trading venue handles the private order. Let’s dive deeper into how this works:

---

### **Key Concept: Private Orders vs. Public Orders**
- **Private Orders**: These are not displayed on the public order book (e.g., on an exchange or ECN). They remain hidden within a broker's system or in private liquidity pools.
- **Public Orders**: These are visible on the central order book of an exchange or trading venue and accessible to all market participants.

The important thing to understand is that **private orders are not inherently isolated from public orders.** They can interact and result in a trade if certain conditions are met.

---

### **Scenarios Where Private Orders and Public Orders Can Interact**

1. **Broker or System Matches Private Orders with Public Liquidity:**
    - Many brokers use **smart order routing (SOR)** systems. These systems:
        - Check public order books (e.g., on exchanges).
        - Compare public bids and asks with the private orders they hold.
        - If a match is found between a private order and a public order, the broker can fill the private order using the public liquidity.
    - **Example**:
        - A seller places a private order to sell 100 shares at $50 (hidden).
        - A buyer places a public bid at $50 for 100 shares on an exchange.
        - The broker routes the private order to the exchange and fills it using the public bid.

2. **Private Orders Executed via Midpoint Matching:**
    - In some cases, private orders in dark pools or broker systems are matched based on the midpoint of the national best bid and offer (NBBO) from the public market.
    - **Example**:
        - Public market: Best bid = $49.90, Best ask = $50.10.
        - A private buyer agrees to buy at the midpoint ($50.00), and a private seller agrees to sell at the midpoint.
        - The trade occurs without appearing in the public market.

3. **Public Orders Sweeping Private Liquidity:**
    - Some trading venues or brokers allow public orders to "sweep" private liquidity pools in search of better prices.
    - **Example**:
        - A public buyer submits a bid for 100 shares at $50.
        - The broker holding a private sell order for 100 shares at $50 matches the two orders and executes the trade.

---

### **Scenarios Where Private Orders and Public Orders Do *Not* Interact**
There are situations where private orders will not interact with public orders due to limitations or preferences set by the broker or trader:

1. **Broker Internal Matching Only:**
    - If a private order is designated to execute only within the broker's internal system or a private dark pool, it won't interact with public orders unless explicitly routed to the public market.

2. **Execution Preferences:**
    - Some private orders are configured with specific instructions (e.g., "do not route to public exchanges"). In this case, even if a matching public order exists, the private order won't interact with it.

3. **Latency or Timing Issues:**
    - If a public bid or ask briefly matches the price of a private order but disappears before the broker routes the private order, no trade will occur.

---

### **Why Use Private Orders If They Can Interact with Public Orders?**
Private orders are used for specific advantages, even though they can interact with public orders under certain conditions:

1. **Minimizing Market Impact:**
    - Large orders are kept private to avoid signaling intentions to the market.
    - Example: A large buy order might push prices higher if made public, so it is kept private and executed in smaller pieces.

2. **Access to Dark Pool Liquidity:**
    - Traders gain access to non-public liquidity pools where trades can occur at better prices or without affecting public markets.

3. **Anonymity:**
    - Private orders prevent other traders from identifying who is placing the order, which can protect trading strategies.

---

### **Summary:**
- **Private orders and public orders are not completely separated.**
- They *can* interact if the broker's system allows it, such as routing private orders to public markets or matching public liquidity with private orders.
- However, the interaction depends on:
    - The broker's execution policy.
    - How the private order is configured (e.g., execution restrictions).
    - The systems or venues used (e.g., dark pools, internal matching engines).

If a private order is hidden and not routed to the public market, it might not interact with public orders—even if the prices match. This is why traders need to carefully specify how they want their orders handled, depending on their execution priorities (e.g., avoiding market impact versus maximizing fill rates).

