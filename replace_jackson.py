import os
import re

def main():
    skip_files = [
        "VitamJacksonMapper.java",
    ]
    
    # We want to keep com.fasterxml.jackson where it's used for AdminExternalClient, etc.
    # Actually, we can just replace everything and then fix the clients manually or use the mapper.
    
    for root, dirs, files in os.walk("."):
        for file in files:
            if file.endswith(".java") and file not in skip_files:
                path = os.path.join(root, file)
                with open(path, "r", encoding="utf-8") as f:
                    content = f.read()
                
                # We need to replace com.fasterxml.jackson with tools.jackson
                if "com.fasterxml.jackson" in content:
                    new_content = content.replace("com.fasterxml.jackson", "tools.jackson")
                    with open(path, "w", encoding="utf-8") as f:
                        f.write(new_content)
                    print(f"Replaced in {path}")

if __name__ == "__main__":
    main()
