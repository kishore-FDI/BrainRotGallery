from app import app as flask_app

# Required by Vercel to hook into your Flask app
def handler(environ, start_response):
    return flask_app.wsgi_app(environ, start_response)
