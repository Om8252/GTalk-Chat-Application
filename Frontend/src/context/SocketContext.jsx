import React, { createContext, useEffect, useRef } from "react";

export const SocketContext = createContext(null);

export function SocketProvider({ children }) {
  const socketRef = useRef(null);

  useEffect(() => {
    // WebSocket-ready mock (Socket.io compatible shape)
    socketRef.current = {
      connected: true,
      on: (event, cb) => {
        console.log("[socket:on]", event);
      },
      emit: (event, payload) => {
        console.log("[socket:emit]", event, payload);
      },
      disconnect: () => {
        console.log("[socket:disconnect]");
      },
    };

    return () => {
      socketRef.current?.disconnect();
    };
  }, []);

  return (
    <SocketContext.Provider value={socketRef.current}>
      {children}
    </SocketContext.Provider>
  );
}
