Here’s a sample code snippet using **Material-UI (MUI)** to create a confirmation popup dialog before submitting data to a server. The example uses a `Dialog` component for the popup and handles user confirmation with buttons.

```jsx
import React, { useState } from "react";
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from "@mui/material";

const ConfirmSubmit = () => {
  const [open, setOpen] = useState(false);

  // Open the confirmation dialog
  const handleClickOpen = () => {
    setOpen(true);
  };

  // Close the confirmation dialog
  const handleClose = () => {
    setOpen(false);
  };

  // Function to handle form submission
  const handleSubmit = () => {
    setOpen(false); // Close the dialog
    // Simulate submitting data to the server
    console.log("Form submitted to the server!");
  };

  return (
    <div>
      {/* Button to open confirmation dialog */}
      <Button variant="contained" color="primary" onClick={handleClickOpen}>
        Submit
      </Button>

      {/* Confirmation dialog */}
      <Dialog
        open={open}
        onClose={handleClose}
        aria-labelledby="confirm-dialog-title"
        aria-describedby="confirm-dialog-description"
      >
        <DialogTitle id="confirm-dialog-title">{"Confirm Submission"}</DialogTitle>
        <DialogContent>
          <DialogContentText id="confirm-dialog-description">
            Are you sure you want to submit this data to the server?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose} color="secondary">
            Cancel
          </Button>
          <Button onClick={handleSubmit} color="primary" autoFocus>
            Confirm
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  );
};

export default ConfirmSubmit;
```

### Key Points:
1. **Dialog Component**:
   - The `Dialog` component from MUI is used to show a modal popup.
   - It includes `DialogTitle`, `DialogContent`, `DialogContentText`, and `DialogActions` for structured content.

2. **State Management**:
   - `open` is a state variable that controls whether the dialog is displayed.
   - `setOpen(true)` opens the dialog, while `setOpen(false)` closes it.

3. **Submit Logic**:
   - The `handleSubmit` function represents the logic for submitting the form. Replace `console.log` with your API call or form submission logic.

4. **Styling**:
   - You can customize the buttons and dialog further using MUI theme overrides or inline styles.

This is a reusable and clean approach to implement a confirmation dialog before submitting data.