import urllib.request
import tarfile
import os
import shutil

url = "https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-arm64-static.tar.xz"
tar_path = "ffmpeg.tar.xz"
out_dir = r"f:\New app\OmniDown\app\src\main\jniLibs\arm64-v8a"

print("Downloading...")
urllib.request.urlretrieve(url, tar_path)
print("Extracting...")
with tarfile.open(tar_path, "r:xz") as tar:
    for member in tar.getmembers():
        if member.name.endswith("/ffmpeg"):
            print(f"Extracting {member.name}...")
            member.name = os.path.basename(member.name)
            tar.extract(member, ".")
            break

os.makedirs(out_dir, exist_ok=True)
dest = os.path.join(out_dir, "libffmpeg.so")
shutil.move("ffmpeg", dest)
print(f"Moved to {dest}. Size: {os.path.getsize(dest)}")
