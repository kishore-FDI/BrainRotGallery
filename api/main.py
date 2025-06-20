from flask import Flask, jsonify, request
from flask_cors import CORS
import logging
from tenacity import retry, stop_after_attempt, wait_fixed, retry_if_exception_type
import yt_dlp

app = Flask(__name__)
CORS(app)

logging.basicConfig(level=logging.INFO)

# Custom exception
class UnsupportedPlatformException(Exception): pass

@app.route('/', methods=['GET', 'POST'])
def home():
    if request.method == 'POST':
        body = request.get_json()
        if not body or "url" not in body:
            return jsonify({'response': 'Missing "url" key in request body'}), 400

        src = body["url"]
        try:
            response = handle_url(src)
            return jsonify({'response': response})
        except UnsupportedPlatformException as e:
            return jsonify({"response": str(e)}), 400
        except Exception as e:
            logging.exception("Unexpected error occurred")
            return jsonify({"response": "Something went wrong"}), 500

    return jsonify({'message': 'This is a GET request.'})

def handle_url(src: str) -> str:
    if "www.youtube.com" in src:
        return "Sorry YouTube isn't working. Use yt_dlp with your account cookies or try https://github.com/kishore-FDI/BrainRotGallery"
    elif "www.instagram.com" in src:
        return handle_instagram(src)
    elif "pornhub.org" in src or "pornhub.com" in src:
        return handle_pornhub(src)
    elif "twitter.com" in src or "x.com" in src:
        return handle_twitter(src)
    elif "tiktok.com" in src:
        return handle_tiktok(src)
    elif "reddit.com" in src:
        return handle_reddit(src)
    else:
        raise UnsupportedPlatformException(f"Haven't started support for {src}. Contact me for support.")

@retry(stop=stop_after_attempt(3), wait=wait_fixed(2), retry=retry_if_exception_type(Exception))
def handle_instagram(src: str) -> str:
    import instaloader
    shortcode = src.strip("/").split("/")[4]
    loader = instaloader.Instaloader()
    post = instaloader.Post.from_shortcode(loader.context, shortcode)
    return post.video_url if post.is_video else "This post is not a video."

@retry(stop=stop_after_attempt(3), wait=wait_fixed(2), retry=retry_if_exception_type(Exception))
def handle_pornhub(src: str) -> str:
    import phub
    from phub import Quality
    client = phub.Client()
    video = client.get(src)
    return video.get_direct_url(Quality.BEST)

@retry(stop=stop_after_attempt(3), wait=wait_fixed(2), retry=retry_if_exception_type(Exception))
def handle_twitter(src: str) -> str:
    ydl_opts = {
        'quiet': True,
        'skip_download': True,
        'forceurl': True,
        'format': 'bestvideo+bestaudio/best',
    }
    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        info = ydl.extract_info(src, download=False)
        return info.get('url', 'Could not extract video URL.')

@retry(stop=stop_after_attempt(3), wait=wait_fixed(2), retry=retry_if_exception_type(Exception))
def handle_tiktok(src: str) -> str:
    ydl_opts = {
        'quiet': True,
        'skip_download': True,
        'forceurl': True,
        'format': 'bestvideo+bestaudio/best',
    }
    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        info = ydl.extract_info(src, download=False)
        return info.get('url', 'Could not extract video URL.')

@retry(stop=stop_after_attempt(3), wait=wait_fixed(2), retry=retry_if_exception_type(Exception))
def handle_reddit(src: str) -> str:
    ydl_opts = {
        'quiet': True,
        'skip_download': True,
        'forceurl': True,
        'format': 'bestvideo+bestaudio/best',
    }
    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        info = ydl.extract_info(src, download=False)
        return info.get('url', 'Could not extract video URL.')

if __name__ == "__main__":
    app.run(debug=True)
