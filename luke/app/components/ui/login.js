"use client";
import React from "react";

function LoginBttn() {
    const showAlert = () => {
        alert("Login clicked!");
    };

    return(
        <button onClick={showAlert}>Login</button>

    );
}

export default LoginBttn;