import { Field, ObjectType } from '@nestjs/graphql';

@ObjectType()
export class PaginationOutput {
    @Field()
    page: number;
    @Field()
    pageSize: number;
    @Field()
    totalItems: number;
    @Field()
    pagesToTheLeft: number;
    @Field()
    pagesToTheRight: number;
    @Field()
    totalPages: number;
    @Field()
    hasPreviousPage: boolean;
    @Field()
    hasNextPage: boolean;

    constructor(page: number, pageSize: number, totalItems: number) {
        this.page = page;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = Math.ceil(totalItems / pageSize);
        this.hasPreviousPage = page > 1;
        this.hasNextPage = page < this.totalPages;
        this.pagesToTheLeft = page - 1;
        this.pagesToTheRight = this.totalPages - page;
    }
}
