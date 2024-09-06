import { Field, InputType } from '@nestjs/graphql';

@InputType()
export class PaginationInput {
    @Field()
    page: number;
    @Field()
    pageSize: number;

    constructor(page: number, pageSize: number) {
        this.page = page;
        this.pageSize = pageSize;
    }
}
