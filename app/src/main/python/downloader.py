import yt_dlp
import json

def get_video_info(url):
    ydl_opts = {
        'quiet': True,
        'no_warnings': True,
        'extract_flat': 'in_playlist',
        'extractor_args': {'youtube': ['player_client=android,web']},
        'http_headers': {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        }
    }
    
    try:
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info = ydl.extract_info(url, download=False)
            
            result = {
                'title': info.get('title', 'Unknown Title'),
                'duration': str(info.get('duration', 'Unknown')),
                'thumbnail': info.get('thumbnail', ''),
                'extractor': info.get('extractor', 'Unknown'),
                'formats': []
            }
            
            formats_dict = {}
            for f in info.get('formats', []):
                vcodec = f.get('vcodec', '')
                ext = f.get('ext', '')
                height = f.get('height')
                format_id = f.get('format_id')
                
                if vcodec != 'none' and height:
                    res = f"{height}p"
                    score = 0
                    if 'avc' in vcodec: score += 10 
                    if ext == 'mp4': score += 5      
                    
                    if res not in formats_dict or score > formats_dict.get(res, {}).get('score', -1):
                        formats_dict[res] = {
                            'id': format_id,
                            'res': res,
                            'ext': ext,
                            'score': score
                        }
                        
            sorted_formats = sorted(formats_dict.values(), key=lambda x: int(x['res'].replace('p','')), reverse=True)
            result['formats'] = [{'format_id': f['id'], 'resolution': f['res'], 'ext': f['ext']} for f in sorted_formats]
            
            result['formats'].append({
                'format_id': 'bestaudio',
                'resolution': 'Audio Only',
                'ext': 'm4a'
            })
            
            return json.dumps({"status": "success", "data": result})
            
    except Exception as e:
        return json.dumps({"status": "error", "message": str(e)})

def download_video(url, output_path, format_id, progress_callback=None):
    def progress_hook(d):
        if d['status'] == 'downloading':
            percent_str = d.get('_percent_str', '0%').replace('\x1b[0;94m', '').replace('\x1b[0m', '').strip()
            speed_str = d.get('_speed_str', 'Unknown')
            downloaded_bytes = d.get('_downloaded_bytes_str', '0')
            total_bytes = d.get('_total_bytes_str', '0')
            
            if progress_callback:
                progress_callback.invoke(f"{percent_str}|{speed_str}|{downloaded_bytes}/{total_bytes}")
                
        elif d['status'] == 'finished':
            if progress_callback:
                progress_callback.invoke("100%|Finished|Done")

    ydl_opts = {
        'outtmpl': f'{output_path}/%(title)s.%(ext)s',
        'progress_hooks': [progress_hook],
        'quiet': True,
        'no_warnings': True,
        'nocheckcertificate': True,
        'extractor_args': {'youtube': ['player_client=android,web']},
        'http_headers': {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        },
        'retries': 15,
        'fragment_retries': 15,
        'continuedl': True,
    }
    
    if format_id == 'bestaudio':
        ydl_opts['format'] = 'bestaudio[ext=m4a]/bestaudio/best'
    else:
        if format_id == '480':
            ydl_opts['format'] = 'bestvideo[height<=480]+bestaudio/best[height<=480]'
        elif format_id == 'best':
            ydl_opts['format'] = 'bestvideo[vcodec^=avc]+bestaudio[ext=m4a]/best[ext=mp4]/best'
        else:
            ydl_opts['format'] = f"{format_id}+bestaudio[ext=m4a]/{format_id}+bestaudio/best"

    try:
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            ydl.download([url])
        return json.dumps({"status": "success", "message": "Download complete"})
    except Exception as e:
        return json.dumps({"status": "error", "message": str(e)})
