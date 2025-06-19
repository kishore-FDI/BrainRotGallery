from flask import Flask, jsonify, request
from flask_cors import CORS
app = Flask(__name__)
CORS(app)

@app.route('/', methods=['GET', 'POST'])
def home():
    if request.method == 'POST':
        body = request.get_json()

        if not body or "url" not in body:
            return jsonify({'response':'Failed to fetch the message. Attach the url to the request with \'url\' as the key'}),400
        
        src=body["url"]

        if "youtu.be" or "www.youtube.com" in src:
            return jsonify({'response': "Sorry Youtube isn't working. You can still yt_dlp with your own account's cookies or use my app https://github.com/kishore-FDI/BrainRotGallery"})
        
        elif "www.instagram.com" in src:
            return jsonify({'response':'Instagram'})
        
        elif "pornhub.org" in src or "pornhub.com" in src:
            return jsonify({'response':'Pornhub'})
        
        else:
            return jsonify({"response":f"Havent started support for {src}. Feel free to approach me for support for the site"}),400

    else:
        return jsonify({'message': 'This is a GET request.'})

# if __name__ == "__main__":
#     app.run(debug=True)