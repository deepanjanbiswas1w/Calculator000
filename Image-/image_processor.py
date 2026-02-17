from PIL import Image, ImageOps, ImageDraw, ImageFont, ImageStat
import io
import datetime
import random
import os
from config import OVERLAYS, FRAMES

def process_image(data, downloaded_file):
    img = Image.open(io.BytesIO(downloaded_file)).convert("RGBA")
    width, height = img.size

    final_format = data['format']
    final_filename = f"edited.{final_format.lower()}"

    mode = data['mode']

    if mode in ["logo", "rounded"]:
        size = (512, 512)
        img = ImageOps.fit(img, size, Image.LANCZOS)
        
        mask = Image.new('L', size, 0)
        draw = ImageDraw.Draw(mask)
        if mode == "logo":
            draw.ellipse((0, 0) + size, fill=255)
        else:
            draw.rounded_rectangle((0, 0) + size, radius=80, fill=255)
        
        result = Image.new('RGBA', size, (0, 0, 0, 0))
        result.paste(img, (0, 0), mask=mask)
        img = result

    elif mode == "screenshot" and data.get('clean_status', False):
        draw = ImageDraw.Draw(img, "RGBA")
        status_h = int(height * 0.055)
        
        # Dominant color extraction
        top_crop = img.crop((0, 0, width, status_h))
        stat = ImageStat.Stat(top_crop)
        r, g, b = [int(x) for x in stat.median[:3]]
        brightness = (r * 299 + g * 587 + b * 114) / 1000
        icon_color = (255, 255, 255, 220) if brightness < 140 else (40, 40, 40, 220)
        
        # Overlay instead of drawing
        style = data.get('status_style', 'ios_light')
        overlay_path = OVERLAYS.get(style, OVERLAYS['ios_light'])
        
        if os.path.exists(overlay_path):
            try:
                overlay = Image.open(overlay_path).convert("RGBA")
                overlay = overlay.resize((width, status_h), Image.LANCZOS)
                img.paste(overlay, (0, 0), overlay)
            except Exception:
                draw.rectangle((0, 0, width, status_h), fill=(r, g, b, 255))
        else:
            # Fallback: সলিড কালার দিয়ে ফিল করা
            draw.rectangle((0, 0, width, status_h), fill=(r, g, b, 255))

        # Dynamic time
        now = datetime.datetime.now().strftime("%I:%M")
        try:
            font_size = int(status_h * 0.55)
            font = ImageFont.truetype("DejaVuSans.ttf", font_size)
        except:
            font = ImageFont.load_default()
        
        # টাইম ড্রয়িং (আইফোনের মতো পজিশন)
        draw.text((width * 0.04, status_h * 0.18), now, fill=icon_color, font=font)

    # Device mockup
    if data.get('mockup_device'):
        frame_path = FRAMES.get(data['mockup_device'])
        if frame_path and os.path.exists(frame_path):
            frame = Image.open(frame_path).convert("RGBA")
            f_width, f_height = frame.size
            # মকআপের স্ক্রিন এরিয়া সেট করা (এটি তোর ফ্রেম পিএনজি অনুযায়ী অ্যাডজাস্ট করতে হবে)
            # সাধারণত ফ্রেমের মাপে স্ক্রিনশট রিসাইজ করে মাঝখানে বসানো হয়
            img_resized = ImageOps.fit(img, (int(f_width*0.88), int(f_height*0.90)), Image.LANCZOS)
            
            # ফ্রেমের মাঝখানে ইমেজ পেস্ট করা
            offset = ((f_width - img_resized.width) // 2, (f_height - img_resized.height) // 2)
            temp_bg = Image.new('RGBA', frame.size, (0, 0, 0, 0))
            temp_bg.paste(img_resized, offset)
            img = Image.alpha_composite(temp_bg, frame)

    output = io.BytesIO()
    save_kwargs = {}
    if final_format == "JPEG":
        img = img.convert("RGB")
        save_kwargs['quality'] = data.get('quality', 85)
    elif final_format == "WEBP":
        save_kwargs['quality'] = data.get('quality', 85)
    
    img.save(output, format=final_format, **save_kwargs)
    output.seek(0)
    
    return output, final_filename
