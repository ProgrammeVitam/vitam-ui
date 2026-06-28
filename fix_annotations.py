import os

def main():
    for root, dirs, files in os.walk("."):
        for file in files:
            if file.endswith(".java"):
                path = os.path.join(root, file)
                with open(path, "r", encoding="utf-8") as f:
                    content = f.read()
                
                if "tools.jackson.annotation" in content:
                    new_content = content.replace("tools.jackson.annotation", "com.fasterxml.jackson.annotation")
                    with open(path, "w", encoding="utf-8") as f:
                        f.write(new_content)
                    print(f"Fixed annotations in {path}")

if __name__ == "__main__":
    main()
