import { Module, Post } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { MyService } from './app.myservice';
import { PostController } from './post.controller';

@Module({
    controllers: [AppController, PostController],
    providers:[AppService, MyService]
})
export class AppModule {

}