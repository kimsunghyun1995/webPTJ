<template>
  <v-card>
    <v-toolbar dark flat color="#0277BD">
      <v-btn icon dark @click="changeDialog">
        <v-icon>mdi-close</v-icon>
      </v-btn>
      <v-toolbar-title>새 글 작성</v-toolbar-title>
      <v-spacer></v-spacer>
    </v-toolbar>
    <v-container style="padding:3%;">
      <div class="d-flex justify-center">
        <v-chip-group
          v-model="teamcheck"
          active-class="blue darken-3 white--text text--accent-4"
          mandatory
          class="my-3"
        >
          <v-chip large v-for="type in types" :key="type" :value="type" class="mx-2">{{ type }} 등록</v-chip>
        </v-chip-group>
      </div>
      <v-col cols="12" sm="12">
        <span class="subheader">✔ 모집 유형</span>
        <v-radio-group v-model="category" row>
          <v-radio label="스터디" color="orange darken-3" value="스터디"></v-radio>
          <v-radio label="프로젝트" color="red darken-3" value="프로젝트"></v-radio>
          <v-radio label="공모전" color="indigo darken-3" value="공모전"></v-radio>
        </v-radio-group>
      </v-col>
      <span class="ml-3 subheader" v-if="teamcheck == '팀'">✔ 팀 소개</span>
      <span class="ml-3 subheader" v-else>✔ 자기 소개</span>
      <v-form ref="form">
        <v-card-text>
          <v-text-field filled dense v-model="teamName" label="팀명" required v-if="teamcheck == '팀'"></v-text-field>
          <v-text-field filled dense v-model="title" label="제목" required></v-text-field>
          <v-textarea filled dense v-model="content" label="내용"></v-textarea>

          <v-combobox
            filled
            dense
            v-model="techStack"
            :items="items"
            hide-selected
            label="필요 기술 스택"
            multiple
            persistent-hint
            small-chips
            v-if="teamcheck == '팀'"
          ></v-combobox>

          <v-combobox
            v-model="memberstack"
            :items="items"
            hide-selected
            label="보유 기술 스택"
            multiple
            persistent-hint
            small-chips
            filled
            v-if="teamcheck == '팀원'"
          ></v-combobox>
          <v-combobox
            filled
            dense
            v-model="memberEmails"
            :items="allmemberEmails"
            hide-selected
            label="참여 확정 팀원 이메일"
            multiple
            persistent-hint
            small-chips
            v-if="teamcheck == '팀'"
          ></v-combobox>
        </v-card-text>
        <!-- <v-flex
          xs12
          class="text-xs-center text-sm-center text-md-center text-lg-center"
        >-->

        <v-card-text>
          <span class="subheader">✔ 관련 사진 업로드</span>
          <v-row>
            <v-col cols="12" lg="6" class="py-0">
              <img class="mt-2" :src="imageUrl" height="150" v-if="imageUrl" />
            </v-col>
          </v-row>
          <v-row>
            <v-col cols="12" class="py-0">
              <v-text-field
                label="사진 선택"
                @click="pickFile"
                v-model="imageName"
                prepend-icon="mdi-camera"
              ></v-text-field>
              <input
                type="file"
                style="display: none;"
                ref="image"
                accept="image/*"
                @change="onFilePicked"
              />
            </v-col>
          </v-row>
        </v-card-text>
        <!-- </v-flex> -->
        <v-card-text class="py-0">
          <v-row>
            <v-col cols="12" lg="6" v-if="teamcheck == '팀'" class="py-0">
              <span class="subheader">✔ 모집 팀원 수</span>
              <v-overflow-btn
                outlined
                v-model="groupSize"
                :rules="groupsizeRules"
                :items="dropdown_font"
                label="선택하세요"
                class="mt-2 pl-0"
              ></v-overflow-btn>
            </v-col>
            <v-col sm="12" lg="6" v-if="teamcheck == '팀'" class="py-0">
              <span class="subheader">✔ 모집 마감일</span>
              <v-menu
                ref="menu"
                v-model="menu"
                :close-on-content-click="false"
                :return-value.sync="date"
                transition="scale-transition"
                offset-y
                min-width="290px"
              >
                <template v-slot:activator="{ on, attrs }">
                  <v-text-field
                    outlined
                    v-model="date"
                    readonly
                    v-bind="attrs"
                    v-on="on"
                    class="mt-2"
                  ></v-text-field>
                </template>
                <v-date-picker v-model="date" no-title scrollable>
                  <v-spacer></v-spacer>
                  <v-btn text color="#0277BD" @click="menu = false">취소</v-btn>
                  <v-btn text color="#0277BD" @click="$refs.menu.save(date)">선택</v-btn>
                </v-date-picker>
              </v-menu>
            </v-col>
          </v-row>
        </v-card-text>
      </v-form>
    </v-container>
    <v-divider></v-divider>
    <v-card-actions>
      <v-btn text color="blue darken-3" @click="changeDialog">취소</v-btn>
      <v-spacer></v-spacer>
      <v-btn
        color="blue darken-3"
        text
        class="font-weight-bold"
        v-if="teamcheck == '팀'"
        @click="submit"
      >팀 등록</v-btn>
      <v-btn
        color="blue darken-3"
        text
        class="font-weight-bold"
        v-if="teamcheck == '팀원'"
        @click="submit"
      >팀원 등록</v-btn>
    </v-card-actions>
  </v-card>
</template>

<script>
import axios from "axios";
// Firebase App (the core Firebase SDK) is always required and must be listed first
import * as firebase from "firebase/app";

export default {
  data: () => ({
    date: new Date().toISOString().substr(0, 10),
    menu: false,
    model: false,
    menu2: false,
    dropdown_font: [3, 4, 5, 6],
    categoryList: ["스터디", "프로젝트", "공모전"],
    items: [
      "C",
      "C++",
      "JAVA",
      "Spring",
      "Django",
      "C#",
      "Go",
      "R",
      "Javascript",
      "vue.js",
      "Linux",
      "MYSQL",
      "Mariadb",
      "RestApi",
      "Python",
      "Pytorch",
      "Tensorflow",
      "Spirngboot",
      "Ruby",
    ],
    teamName: null,
    title: null,
    content: null,
    groupSize: null,
    category: "",
    techStack: [],
    memberstack: [],
    email: null,
    teamcheck: null,
    idx: null,
    memberEmails: [],
    allmemberEmails: [],
    name: "",
    types: ["팀", "팀원"],

    // 이미지를 저장할 변수들
    imageName: "", // 이미지 파일 이름
    imageUrl: "", // 이미지 파일 경로
    imageFile: "", // 이미지 파일 객체
  }),
  computed: {
    groupsizeRules() {
      const rules = [
        (v) => !!v || "모집 인원수를 선탁해야합니다.",
        (v) =>
          v > this.memberEmails.length ||
          "모집 인원이 입력한 팀원수 보다 값이 작습니다.",
      ];
      return rules;
    },
  },

  components: {},
  created() {
    let token = window.$cookies.get("nnd");
    if (token) {
      this.email = token.object.email;
      this.idx = token.object.idx;
      this.name = token.object.name;
      this.memberstack = JSON.parse(token.object.memberstack);
    }
    axios
      .get(`${process.env.VUE_APP_API_URL}/member/all`, {
        headers: {
          Authorization: "Bearer " + token.data, // the token is a variable which holds the token
        },
      })
      .then((response) => {
        this.allmemberEmails = response.data;
      })
      .catch((error) => {
        console.log(error.response);
        alert("등록에 실패했습니다.");
      });
  },
  methods: {
    pickFile() {
      this.$refs.image.click();
    },

    onFilePicked(e) {
      const files = e.target.files;
      if (files[0] !== undefined) {
        this.imageName = files[0].name;
        if (this.imageName.lastIndexOf(".") <= 0) {
          return;
        }
        const fr = new FileReader();
        fr.readAsDataURL(files[0]);
        fr.addEventListener("load", () => {
          this.imageUrl = fr.result;
          this.imageFile = files[0]; // this is an image file that can be sent to server...
        });
      } else {
        this.imageName = "";
        this.imageFile = "";
        this.imageUrl = "";
      }
    },

    submit() {
      var obj, url;
      let token = window.$cookies.get("nnd");
      if (this.teamcheck == "팀") {
        obj = {
          email: this.email,
          teamName: this.teamName,
          memberEmails: JSON.stringify(this.memberEmails),
          groupSize: this.groupSize,
          deadLine: this.date,
          title: this.title,
          content: this.content,
          techStack: JSON.stringify(this.techStack),
          category: this.category,
          name: this.name,
          imageurl: this.imageName,
        };
        url = `${process.env.VUE_APP_API_URL}/teamboard/save/${this.idx}`;
      } else {
        // 팀원의 경우
        obj = {
          email: this.email,
          title: this.title,
          content: this.content,
          category: this.category,
          techStack: JSON.stringify(this.memberstack),
          name: this.name,
          imageurl: this.imageName,
        };
        url = `${process.env.VUE_APP_API_URL}/memberboard/save/${this.idx}`;
      }
      axios
        .put(url, obj, {
          headers: {
            Authorization: "Bearer " + token.data, // the token is a variable which holds the token
          },
        })
        .then((response) => {
          // axios 요청등록 후 리턴받은 teamboardNo값을 route로 해서 firebase에 image 등록
          if (this.imageName != "") {
            // 이미지 파일을 쓰는 경우만 파이어베이스 코드를 진행토록 한다.
            var image_url = `images/${
              this.teamcheck == "팀" ? "team" : "member"
            }/${response.data}/${this.imageName}`;

            const storageRef = firebase.storage().ref();

            storageRef
              .child(image_url)
              .put(this.imageFile)
              .on(
                "state_changed",
                (snapshot) => {
                  console.log(snapshot);
                },
                (error) => {
                  console.log(error);
   
                },
                () => {
                  alert("등록에 성공했습니다.");

                  // 등록페이지 초기화
                  this.teamName = null;
                  this.title = null;
                  this.content = null;
                  this.groupSize = null;
                  this.category = "";
                  this.techStack = [];
                  this.memberstack = [];
                  this.date = new Date().toISOString().substr(0, 10);
                  this.imageName = ""; // 이부분 컴플릿트 나는지 조심(다른 사람이 이작업을 이미 했으므로)
                  this.imageFile = "";
                  this.imageUrl = "";

                  this.changeDialog();
                  this.goMain();
                }
              );
          } else {
            alert("등록에 성공했습니다.");
            this.changeDialog();
            this.goMain();
          }
        })
        .catch((error) => {
          console.log(error.response);
          alert("등록에 실패했습니다.");
        });
    },
    goMain() {
      if (this.$route.path == "/") {
        this.$router.go();
      } else {
        this.$router.push("/");
      }
    },
    changeDialog() {
      this.$emit("changeDialog");
    },
  },
};
</script>

<style scoped>
.tc {
  text-align: center;
}

.subheader {
  padding: 1px 0;
  background-color: #eeeeee;
  font-style: italic;
}
</style>
