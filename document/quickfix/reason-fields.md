# Reason Fields

In the **FIX (Financial Information Exchange) protocol**, there is no explicit, single field that is universally designated as an "error code" field to indicate categorized error types. However, certain fields are commonly used to convey error-related information, depending on the context (e.g., session-level or application-level messages). Here's an overview:

---

### **1. Session-Level Errors**
For session-level errors, the **Session Reject (MsgType = 3)** message is used to indicate problems like invalid message format, missing fields, or sequence number issues. The following fields are used to provide details about the error:

- **RefTagID (Tag 371)**:
    - Specifies the tag number of the field causing the error.
    - Example: If a required field is missing or has invalid data, `RefTagID` will indicate the problematic field.

- **SessionRejectReason (Tag 373)**:
    - Provides a numeric code categorizing the type of error.
    - Common values:
        - `0`: Invalid tag number
        - `1`: Required tag missing
        - `2`: Tag not defined for this message type
        - `3`: Undefined tag
        - `5`: Value is incorrect (out of range or invalid)
        - `6`: Incorrect data format for value
        - `11`: Invalid message type
    - Full list of `SessionRejectReason` codes can be found in the FIX protocol specification.

- **Text (Tag 58)**:
    - Optional field used to provide human-readable details about the error.

---

### **2. Application-Level Errors**
For application-level errors, the **Reject (MsgType = 9)** message is used. This message is typically sent when an application-level problem is encountered, such as a business rule violation. The following fields are used to describe the error:

- **RefSeqNum (Tag 45)**:
    - Specifies the sequence number of the message being rejected.

- **RefTagID (Tag 371)**:
    - Specifies the tag number of the field causing the issue (same as in session-level errors).

- **RefMsgType (Tag 372)**:
    - Indicates the type of message that is being rejected (e.g., `D` for New Order - Single, `F` for Order Cancel Request, etc.).

- **SessionRejectReason (Tag 373)**:
    - Provides a reason code for the rejection (same as in session-level errors).

- **Text (Tag 58)**:
    - Optional field to provide additional explanatory information about the rejection.

---

### **3. Business-Level Errors**
In certain cases, business-level error codes are conveyed in specific business messages. For instance:

- **OrdRejReason (Tag 103)**:
    - Used in the **Execution Report (MsgType = 8)** when an order is rejected.
    - Common values:
        - `0`: Broker/Exchange option
        - `1`: Unknown symbol
        - `2`: Exchange closed
        - `3`: Order exceeds limit
        - `4`: Too late to enter
        - `5`: Unknown order
        - `99`: Other (with additional details in `Text (Tag 58)`)

- **CxlRejReason (Tag 102)**:
    - Used in the **Order Cancel Reject (MsgType = 9)** message to indicate why an order cancel request was rejected.
    - Common values:
        - `0`: Too late to cancel
        - `1`: Unknown order
        - `99`: Other (with additional details in `Text (Tag 58)`)

---

### **Key Fields for Error Codes**
To summarize, these are the key fields that may indicate error codes or categorize error types:

| **Tag**     | **Field Name**        | **Description**                                                                      |
|-------------|-----------------------|--------------------------------------------------------------------------------------|
| **371**     | RefTagID              | Specifies the tag of the problematic field.                                          |
| **373**     | SessionRejectReason   | Categorizes the session-level error with a numeric code.                             |
| **372**     | RefMsgType            | Indicates the type of message causing the rejection.                                 |
| **45**      | RefSeqNum             | Refers to the sequence number of the rejected message.                               |
| **103**     | OrdRejReason          | Business-level error code for order rejections (used in Execution Report).           |
| **102**     | CxlRejReason          | Business-level error code for order cancel rejections (used in Order Cancel Reject). |
| **58**      | Text                  | Provides additional, human-readable details about the error.                         |

---

### **Conclusion**
While there isn't a single field explicitly named "error code" in FIX, fields like `SessionRejectReason (Tag 373)`, `OrdRejReason (Tag 103)`, and `CxlRejReason (Tag 102)` are commonly used to categorize errors. These fields, along with `Text (Tag 58)` for additional context, are the primary mechanisms for reporting and categorizing errors in FIX protocol messages.