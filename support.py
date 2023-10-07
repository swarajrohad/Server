from DeepImageSearch import Load_Data, Search_Setup
import os


def save_image(url_, file_bytes):
    image_name = url_[38:]
    image_name = image_name.replace("/", " ")
    image_name = image_name.replace("?", "{") + (".jpg")
    file_bytes = file_bytes[:-5]
    with open("all\\" + image_name, "wb") as f:
        f.write(file_bytes)
    image_list = Load_Data().from_folder([os.getcwd()+r'\all'])
    st = Search_Setup(image_list, model_name="vgg19", pretrained=True, image_count=None)
    st.add_images_to_index([os.getcwd()+r'\all' + image_name])
    print("file saved\n", decode_name(image_name))


def decode_name(file_path):
    raw = file_path.replace(" ", "/")
    raw = raw.replace("{", "?")
    raw = "https://firebasestorage.googleapis.com" + raw[:-4]
    return raw


def find_similar_image(filename):
    image_list = Load_Data().from_folder(['all'])
    st = Search_Setup(image_list, model_name="vgg19", pretrained=True, image_count=None)
    st.run_index()
    obj = st.get_similar_images(image_path=filename, number_of_images=3)
    matched_uri = []
    temp = ""
    for path in obj.values():
        for i in path[::-1]:
            if i == "\\":
                break
            temp += i
        matched_uri.append(decode_name(temp[::-1]))
    return matched_uri


def find_similar_image2(filename):
    image_list = Load_Data().from_folder(['all'])
    st = Search_Setup(image_list, model_name="vgg19", pretrained=True, image_count=None)
    st.run_index()
    obj = st.get_similar_images(image_path=filename, number_of_images=3)
    matched_uri = []
    for path in obj.values():
        basename = os.path.basename(path)
        matched_uri.append(decode_name(basename))
    return matched_uri


if __name__ == "__main__":
    find_similar_image2("pc1.jpg")
