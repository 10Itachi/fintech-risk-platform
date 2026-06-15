// MagicBackground.jsx
// Hogwarts themed background — castle image + gold particle overlay
// Fixed position, sits behind all content

import { useEffect, useRef } from 'react';
import hogwartsBg from '../../assets/gringotts-bank.avif';

export default function MagicBackground() {
  const canvasRef = useRef(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    const ctx = canvas.getContext('2d');
    let animationId;

    const resize = () => {
      canvas.width = window.innerWidth;
      canvas.height = window.innerHeight;
    };
    resize();
    window.addEventListener('resize', resize);

    const particles = Array.from({ length: 80 }, () => ({
      x: Math.random() * window.innerWidth,
      y: Math.random() * window.innerHeight,
      size: Math.random() * 2.5 + 0.5,
      speedX: (Math.random() - 0.5) * 0.3,
      speedY: -Math.random() * 0.5 - 0.1,
      opacity: Math.random() * 0.6 + 0.1,
      pulse: Math.random() * Math.PI * 2,
    }));

    const draw = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      particles.forEach((p) => {
        p.pulse += 0.02;
        p.x += p.speedX;
        p.y += p.speedY;
        p.opacity = 0.1 + Math.abs(Math.sin(p.pulse)) * 0.5;

        if (p.y < -10) { p.y = canvas.height + 10; p.x = Math.random() * canvas.width; }
        if (p.x < -10) p.x = canvas.width + 10;
        if (p.x > canvas.width + 10) p.x = -10;

        ctx.beginPath();
        ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
        ctx.fillStyle = `rgba(245, 158, 11, ${p.opacity})`;
        ctx.fill();

        ctx.beginPath();
        ctx.arc(p.x, p.y, p.size * 3, 0, Math.PI * 2);
        ctx.fillStyle = `rgba(245, 158, 11, ${p.opacity * 0.12})`;
        ctx.fill();
      });

      animationId = requestAnimationFrame(draw);
    };

    draw();

    return () => {
      cancelAnimationFrame(animationId);
      window.removeEventListener('resize', resize);
    };
  }, []);

  return (
    <>
      {/* Layer 1 — Hogwarts/Gringotts castle image */}
      <div style={{
        position: 'fixed',
        inset: 0,
        zIndex: 0,
        backgroundImage: `url(${hogwartsBg})`,
        backgroundSize: 'cover',
        backgroundPosition: 'center',
        backgroundRepeat: 'no-repeat',
      }} />

      {/* Layer 2 — Dark overlay for readability */}
      <div style={{
        position: 'fixed',
        inset: 0,
        zIndex: 1,
        background: `linear-gradient(
          180deg,
          rgba(10, 15, 30, 0.82) 0%,
          rgba(10, 15, 30, 0.70) 40%,
          rgba(10, 15, 30, 0.88) 100%
        )`,
      }} />

      {/* Layer 3 — Gold vignette edges */}
      <div style={{
        position: 'fixed',
        inset: 0,
        zIndex: 2,
        background: `
          radial-gradient(ellipse at 20% 50%, rgba(120, 53, 15, 0.12) 0%, transparent 60%),
          radial-gradient(ellipse at 80% 20%, rgba(245, 158, 11, 0.06) 0%, transparent 50%)
        `,
      }} />

      {/* Layer 4 — Subtle stone grid */}
      <div style={{
        position: 'fixed',
        inset: 0,
        zIndex: 3,
        backgroundImage: `
          linear-gradient(rgba(245, 158, 11, 0.02) 1px, transparent 1px),
          linear-gradient(90deg, rgba(245, 158, 11, 0.02) 1px, transparent 1px)
        `,
        backgroundSize: '60px 60px',
      }} />

      {/* Layer 5 — Floating gold particles */}
      <canvas
        ref={canvasRef}
        style={{
          position: 'fixed',
          inset: 0,
          zIndex: 4,
          pointerEvents: 'none',
        }}
      />
    </>
  );
}