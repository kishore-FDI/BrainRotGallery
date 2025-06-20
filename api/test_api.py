import requests

API_URL = "http://127.0.0.1:5000/"

TEST_LINKS = [
    # Twitter
    "https://x.com/elonmusk/status/1935814047322325402",
    # TikTok
    "https://www.tiktok.com/@tiktok/video/7106594312292453675",
    # Reddit
    "https://www.reddit.com/r/aww/comments/3g1jfi/otters_holding_hands/",
    # Instagram
    "https://www.instagram.com/reel/DF0ZKwszqWS/?igsh=MWl3bHlyOHF1Z2hiOA==",
    # Pornhub
    "https://www.pornhub.org/view_video.php?viewkey=66bf5f7c1321c",
    # Unsupported
    "https://www.example.com/video/12345"
]

def test_api():
    for link in TEST_LINKS:
        print(f"Testing: {link}")
        try:
            resp = requests.post(API_URL, json={"url": link})
            print(f"Status: {resp.status_code}")
            print(f"Response: {resp.json()}\n")
        except Exception as e:
            print(f"Error: {e}\n")

if __name__ == "__main__":
    test_api() 