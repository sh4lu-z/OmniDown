import yt_dlp
import json

def get_video_info(url):
    ydl_opts = {
        'quiet': True,
        'no_warnings': True,
        'extract_flat': 'in_playlist',
        'replace_in_metadata': [('title', r'(?i)[#@]\S+', '')],
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
                acodec = f.get('acodec', '')
                ext = f.get('ext', '')
                height = f.get('height')
                width = f.get('width')
                format_id = f.get('format_id')
                fps = f.get('fps', 0)
                
                if vcodec != 'none' and (height or width):
                    res_base = f"{height}p"
                    if width:
                        if width >= 3840: res_base = "2160p"
                        elif width >= 2560: res_base = "1440p"
                        elif width >= 1920: res_base = "1080p"
                        elif width >= 1280: res_base = "720p"
                        elif width >= 854: res_base = "480p"
                        elif width >= 640: res_base = "360p"
                        elif width >= 426: res_base = "240p"
                        elif width >= 256: res_base = "144p"
                        
                    res_key = res_base
                    if fps and fps > 40:
                        res_key += str(int(fps))
                        
                    has_audio = acodec != 'none'
                    
                    score = 0
                    if 'avc' in vcodec: score += 10 
                    if ext == 'mp4': score += 5
                    if has_audio: score += 2 
                    
                    if res_key not in formats_dict or score > formats_dict[res_key]['score']:
                        label = res_key
                        cmp_val = width if width else (height or 0)
                        if cmp_val >= 3840: label = f"4K"
                        elif cmp_val >= 2560: label = f"2K"
                        elif cmp_val >= 1280: label = f"{res_key} HD"
                        
                        formats_dict[res_key] = {
                            'id': format_id,
                            'res': label,
                            'sort_val': cmp_val,
                            'ext': 'mp4',
                            'score': score,
                            'needs_merge': not has_audio
                        }
                        
            sorted_formats = sorted(formats_dict.values(), key=lambda x: x['sort_val'], reverse=True)
            result['formats'] = [{'format_id': f['id'], 'resolution': f['res'], 'ext': f['ext'], 'needs_merge': f.get('needs_merge', False)} for f in sorted_formats]
            
            result['formats'].append({
                'format_id': 'bestaudio',
                'resolution': 'Audio Only',
                'ext': 'm4a',
                'needs_merge': False
            })
            
            return json.dumps({"status": "success", "data": result})
            
    except Exception as e:
        return json.dumps({"status": "error", "message": str(e)})

def download_video(url, output_path, format_id, filename_override=None, progress_callback=None):
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
        'outtmpl': f'{output_path}/{filename_override}' if filename_override else f'{output_path}/%(title)s.%(ext)s',
        'replace_in_metadata': [('title', r'(?i)[#@]\S+', '')],
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
        ydl_opts['format'] = format_id

    try:
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info_dict = ydl.extract_info(url, download=True)
            filename = ydl.prepare_filename(info_dict)
            title = info_dict.get('title', 'Unknown Title')
            
            if filename_override:
                filename = f"{output_path}/{filename_override}"
                
        return json.dumps({"status": "success", "message": "Download complete", "filename": filename, "title": title})
    except Exception as e:
        return json.dumps({"status": "error", "message": str(e)})
