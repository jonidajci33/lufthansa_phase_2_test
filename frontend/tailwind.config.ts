import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        surface: {
          DEFAULT: '#000',
          secondary: '#111',
          tertiary: '#1a1a1a',
        },
        border: {
          DEFAULT: '#333',
          hover: '#555',
        },
        text: {
          DEFAULT: '#eee',
          secondary: '#999',
          muted: '#666',
        },
        card: {
          front: '#fff',
          back: '#111',
        },
        accent: '#fff',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      perspective: {
        '1000': '1000px',
      },
    },
  },
  plugins: [],
} satisfies Config;
