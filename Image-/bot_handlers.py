import telebot
from telebot import types
from config import API_TOKEN
from image_processor import process_image
import io

bot = telebot.TeleBot(API_TOKEN, threaded=False)
user_data = {}

@bot.message_handler(commands=['start'])
def send_welcome(message):
    bot.reply_to(message, "ওস্তাদ, ছবি পাঠাও! আমি একদম রেডি।\n/cancel দিয়ে রিসেট করো।")

@bot.message_handler(commands=['cancel'])
def cancel(message):
    chat_id = message.chat.id
    user_data.pop(chat_id, None)
    bot.reply_to(message, "সব ক্লিয়ার ওস্তাদ! নতুন ছবি পাঠাতে পারো।")

@bot.message_handler(content_types=['photo'])
def handle_photo(message):
    chat_id = message.chat.id
    user_data[chat_id] = {'photo': message.photo[-1].file_id}
    
    markup = types.InlineKeyboardMarkup(row_width=2)
    markup.add(
        types.InlineKeyboardButton("অ্যাপ লোগো (Circular) 🎨", callback_data="mode_logo"),
        types.InlineKeyboardButton("অ্যাপ লোগো (Rounded) 🔲", callback_data="mode_rounded"),
        types.InlineKeyboardButton("স্ক্রিনশট ক্লিনার 📱", callback_data="mode_screenshot")
    )
    bot.send_message(chat_id, "ছবি পেয়েছি! কী করব ওস্তাদ?", reply_markup=markup)

@bot.callback_query_handler(func=lambda call: True)
def callback_query(call):
    chat_id = call.message.chat.id
    if chat_id not in user_data:
        bot.answer_callback_query(call.id, "পুরনো মেসেজ। নতুন ছবি দাও ওস্তাদ!", show_alert=True)
        return

    data = user_data[chat_id]

    if call.data.startswith("mode_"):
        data['mode'] = call.data.split("_")[1]
        markup = types.InlineKeyboardMarkup(row_width=3)
        markup.add(
            types.InlineKeyboardButton("JPG", callback_data="format_JPEG"),
            types.InlineKeyboardButton("PNG", callback_data="format_PNG"),
            types.InlineKeyboardButton("WebP", callback_data="format_WEBP")
        )
        bot.edit_message_text("কোন ফরম্যাটে লাগবে?", chat_id, call.message.message_id, reply_markup=markup)

    elif call.data.startswith("format_"):
        data['format'] = call.data.split("_")[1]
        if data['mode'] == "screenshot":
            markup = types.InlineKeyboardMarkup(row_width=2)
            markup.add(
                types.InlineKeyboardButton("ক্লিন কর ✅", callback_data="clean_yes"),
                types.InlineKeyboardButton("না ❌", callback_data="clean_no")
            )
            bot.edit_message_text("স্ট্যাটাস বার ক্লিন করব?", chat_id, call.message.message_id, reply_markup=markup)
        else:
            finalize_request(chat_id, call.message)

    elif call.data.startswith("clean_"):
        data['clean_status'] = (call.data.split("_")[1] == "yes")
        if data['clean_status']:
            markup = types.InlineKeyboardMarkup(row_width=2)
            markup.add(
                types.InlineKeyboardButton("iOS Light", callback_data="style_ios_light"),
                types.InlineKeyboardButton("iOS Dark", callback_data="style_ios_dark"),
                types.InlineKeyboardButton("Android", callback_data="style_android")
            )
            bot.edit_message_text("কোন স্টাইলের আইকন বসাব?", chat_id, call.message.message_id, reply_markup=markup)
        else:
            finalize_request(chat_id, call.message)

    elif call.data.startswith("style_"):
        data['status_style'] = call.data[6:]
        finalize_request(chat_id, call.message)

def finalize_request(chat_id, message):
    try:
        bot.edit_message_text("প্রসেসিং চলছে ওস্তাদ... ⏳", chat_id, message.message_id)
        data = user_data[chat_id]
        file_info = bot.get_file(data['photo'])
        downloaded_file = bot.download_file(file_info.file_path)
        
        output, filename = process_image(data, downloaded_file)
        
        bot.send_document(chat_id, output, visible_file_name=filename)
        bot.send_message(chat_id, "কাজ শেষ! 🔥 আবার ছবি পাঠাতে পারো।")
    except Exception as e:
        bot.send_message(chat_id, f"কিছু গণ্ডগোল হয়েছে: {str(e)}")
    finally:
        user_data.pop(chat_id, None)
