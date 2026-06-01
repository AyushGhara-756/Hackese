"use client";
import React from "react";

function SignupBttn() {
    const showAlert = () => {
        alert("Signup clicked!");
    };

    return(
        <button onClick={showAlert}>Signup</button>

    );
}

export default SignupBttn;