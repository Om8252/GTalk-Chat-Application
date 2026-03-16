import { useEffect, useState } from "react";

export default function useResponsive() {
  const [width, setWidth] = useState(window.innerWidth);

  useEffect(() => {
    const handleResize = () => setWidth(window.innerWidth);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  return {
    isMobile: width <= 600,
    isTablet: width > 600 && width <= 1024,
    isDesktop: width > 1024,
    width,
  };
}
