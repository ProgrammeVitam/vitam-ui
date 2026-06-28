import os
import re

def main():
    skip_files = [
        "VitamJacksonMapper.java",
    ]
    
    for root, dirs, files in os.walk("."):
        for file in files:
            if file.endswith(".java") and file not in skip_files:
                path = os.path.join(root, file)
                with open(path, "r", encoding="utf-8") as f:
                    content = f.read()
                
                if "com.fasterxml.jackson" in content:
                    new_content = content.replace("com.fasterxml.jackson", "tools.jackson")
                    # Replace Jackson 2 Exceptions
                    new_content = new_content.replace("tools.jackson.core.JsonParseException", "tools.jackson.core.exc.StreamReadException")
                    new_content = new_content.replace("tools.jackson.databind.JsonMappingException", "tools.jackson.databind.DatabindException")
                    new_content = new_content.replace("tools.jackson.core.JsonProcessingException", "tools.jackson.core.JacksonException")
                    new_content = new_content.replace("JsonParseException", "StreamReadException")
                    new_content = new_content.replace("JsonMappingException", "DatabindException")
                    new_content = new_content.replace("JsonProcessingException", "JacksonException")
                    
                    with open(path, "w", encoding="utf-8") as f:
                        f.write(new_content)
                    print(f"Replaced in {path}")

if __name__ == "__main__":
    main()
