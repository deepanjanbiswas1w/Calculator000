import os

API_TOKEN = 'YOUR_API_TOKEN_HERE'  # BotFather থেকে নেওয়া টোকেন

# Render-এর জন্য webhook
WEBHOOK_PATH = '/botwebhook'
RENDER_NAME = os.environ.get('RENDER_SERVICE_NAME', 'your-service-name')
WEBHOOK_URL = f"https://{RENDER_NAME}.onrender.com{WEBHOOK_PATH}"

# Static files path (Render-এ static/ ফোল্ডারে রাখো)
STATIC_DIR = os.path.join(os.path.dirname(__file__), 'static')

# Overlays & frames (ফাইল নাম)
OVERLAYS = {
    'ios_light': os.path.join(STATIC_DIR, 'ios_status_light.png'),
    'ios_dark': os.path.join(STATIC_DIR, 'ios_status_dark.png'),
    'android': os.path.join(STATIC_DIR, 'android_status.png'),
}

FRAMES = {
    'iphone_15_pro': os.path.join(STATIC_DIR, 'iphone_15_pro_frame.png'),
    'pixel_8': os.path.join(STATIC_DIR, 'pixel_8_frame.png'),
}
