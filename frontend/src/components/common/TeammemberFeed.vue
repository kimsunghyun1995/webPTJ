<template>
  <v-app class="mt-16" style="background-color:#eceff1;">
    <div v-if="this.type == 'team'">
      <!-- <p class="mb-0 ml-3"># 팀보드</p> -->
      <news-feed2 v-for="(board, i) in list" v-bind:teaminfo="list[i]" v-bind:key="i"></news-feed2>
    </div>
    <div v-if="this.type == 'member'">
      <!-- <p class="mb-0 ml-3"># 멤바보드</p> -->
      <news-feed v-for="(board, i) in list" v-bind:teaminfo="list[i]" v-bind:key="i"></news-feed>
    </div>
    <ActionButton />
    <infinite-loading @infinite="infiniteHandler" spinner="waveDots" ref="InfiniteLoading"></infinite-loading>
  </v-app>
</template>

<script>
import InfiniteLoading from "vue-infinite-loading";
import NewsFeed2 from "./NewsFeed2.vue";
import NewsFeed from "./NewsFeed.vue";
import axios from "axios";
import { EventBus } from "../../main.js";
import * as firebase from "firebase/app";
import ActionButton from "./ActionButton.vue";

export default {
  data() {
    return {
      list: [],
      page: 0,
      size: 10,
      option: {},
      type: "team", // 초기 검색 필터값(team으로 초기화됨)
      query: [],
      category: [],
      skills: [],
      idx: null
    };
  },
  created() {
    let token = window.$cookies.get("nnd");
    if (token) {
      this.idx = token.object.idx;
    }

    EventBus.$on("search", obj => {
      this.type = obj.typeSelection;
      this.query = obj.search;
      this.category = obj.categorySelection;
      this.skills = obj.newSkill;
      this.list = [];
      this.$refs.InfiniteLoading.stateChanger.reset();
      this.page = 0;
    });
  },

  methods: {
    infiniteHandler($state) {
      let token = window.$cookies.get("nnd");

      axios
        .put(
          `${process.env.VUE_APP_API_URL}/` +
            this.type +
            "board/search/" +
            this.idx,
          {
            query: this.query,
            category: this.category,
            skills: this.skills
          },
          {
            headers: {
              Authorization: "Bearer " + token.data // the token is a variable which holds the token
            },
            params: {
              page: this.page,
              size: this.size,
              mode: 1
            }
          }
        )
        .then(({ data }) => {
          setTimeout(() => {
            if (data.length) {
              this.page += 1;
              var len = this.list.length; // 무한 스크롤을 진행 할 때마다 list엔 데이터가 쌓이게되므로, len에 변경할 url들이 위치하는 list의 인덱스를 미리 저장한다.
              this.list.push(...data);

              // 새로 들어온 카드들을 대상으로 파이어베이스 작업, 팀원들의 링크들을 가져오는 작업을 진행한다.
              for (let i = len; i < this.list.length; i++) {
                // 여기서 파이어베이스에서 이미지를 얻기 위해 imageurl을 변환한다
                var card = this.list[i];

                if (card.imageurl != "") { // url이 존재하는 경우만 firebase 코드를 진행한다.
                  if (this.type == "team") {
                    card.imageurl = `images/${this.type}/${card.teamboardno}/${card.imageurl}`;
                  } else {
                    card.imageurl = `images/${this.type}/${card.boardno}/${card.imageurl}`;
                  }
                  firebase
                    .storage()
                    .ref()
                    .child(card.imageurl)
                    .getDownloadURL()
                    .then(imageurl => {
                      this.list[i].imageurl = imageurl;
                    })
                    .catch(function(error) {
                      // A full list of error codes is available at
                      // https://firebase.google.com/docs/storage/web/handle-errors
                      switch (error.code) {
                        case "storage/object-not-found":
                          // File doesn't exist
                          break;

                        case "storage/unauthorized":
                          // User doesn't have permission to access the object
                          break;

                        case "storage/canceled":
                          // User canceled the upload
                          break;

                        case "storage/unknown":
                          // Unknown error occurred, inspect the server response
                          break;
                      }
                    });
                }

                //////////////////////////////////////////////////////////////////////
                // 팀원들의 링크를 가져오는 작업을 진행한다.
                // 1. teamboardno를 가지고 axios 요청을 보낸다.
                if (this.type == "team") {
                  axios
                    .get(
                      `${process.env.VUE_APP_API_URL}/teammenu/member/${this.list[i].teamboardno}`,
                      {
                        headers: {
                          Authorization: "Bearer " + token.data // the token is a variable which holds the token
                        }
                      }
                    )
                    .then(({ data }) => {
                      this.list[i].profiles = data;
                      this.list[i].content = this.list[i].content.replace(
                        /\n/g,
                        "<br>"
                      );
                    });
                }
              }
              $state.loaded();
            } else {
              $state.complete();
            }
          }, 1000);
        });
    }
  },
  components: {
    NewsFeed2: NewsFeed2,
    NewsFeed: NewsFeed,
    ActionButton,
    InfiniteLoading
  }
};
</script>

<style></style>
